package com.project.fdsconsumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.common.PaymentDto;
import com.project.common.TransactionFinalizedDto;
import com.project.fdsconsumer.domain.Store;
import com.project.fdsconsumer.domain.Transaction;
import com.project.fdsconsumer.domain.User;
import com.project.fdsconsumer.repository.TransactionRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class FdsService {

    private final StringRedisTemplate redisTemplate;
    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, TransactionFinalizedDto> kafkaTemplate; // ❗️ DTO 타입 변경
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Value("${app.verification-api.url}")
    private String verificationApiUrl;
    public void processTransaction(PaymentDto payment) {
        String userId = payment.getUserId();
        String userLocationKey = "user:" + userId + ":location";
        String lastKnownCountry = redisTemplate.opsForValue().get(userLocationKey);

        if (lastKnownCountry == null) {
            Optional<Transaction> lastTransaction = transactionRepository.findTopByUser_UserIdOrderByTransactionAtDesc(userId);
            if (lastTransaction.isPresent()) {
                lastKnownCountry = lastTransaction.get().getCountry();
                redisTemplate.opsForValue().set(userLocationKey, lastKnownCountry, 24, TimeUnit.HOURS);
            }
        }

        if (lastKnownCountry != null && !lastKnownCountry.equals(payment.getCountry())) {
            saveTransaction(payment, "PENDING");
            String txId = payment.getTransactionId();
            String pendingTxKey = "fds:pending:" + txId;
            try {
                // ❗️ 1. DTO 객체를 JSON 문자열로 변환합니다.
                String paymentJson = objectMapper.writeValueAsString(payment);
                // ❗️ 2. JSON 문자열을 String 타입으로 Redis에 저장합니다.
                redisTemplate.opsForValue().set(pendingTxKey, paymentJson, 5, TimeUnit.MINUTES);
            } catch (JsonProcessingException e) {
                // 실제로는 로깅 처리가 필요합니다.
                System.err.println("Failed to serialize Payment DTO: " + e.getMessage());
                return;
            }
            String verificationLink = verificationApiUrl + "/verify/" + txId;

            String smsMessage = String.format("[국외결제] %s에서 %d원 결제 요청. 확인 링크: %s",
                    payment.getCountry(), payment.getAmount(), verificationLink);
            System.out.println("Sent SMS: " + smsMessage);
            saveTransaction(payment, "PENDING");
        } else {
            redisTemplate.opsForValue().set(userLocationKey, payment.getCountry(), 24, TimeUnit.HOURS);
            saveTransaction(payment, "APPROVED");
            //이벤트 발행
            publishFinalizedEvent(payment, "APPROVED");
            System.out.println("Normal transaction for user: " + userId);
        }
    }

    @Transactional
    public void finalizeTransaction(String txId, String decision) {
        String pendingTxKey = "fds:pending:" + txId;
        // 1. Redis에서 보류 중인 거래의 JSON 정보를 가져옵니다.
        String paymentJson = redisTemplate.opsForValue().get(pendingTxKey);

        // 정보가 없으면 (이미 처리되었거나 만료됨) 즉시 종료
        if (paymentJson == null) {
            System.out.println("Transaction already processed or expired: " + txId);
            return;
        }

        transactionRepository.findByTransactionIdAndStatus(txId,"PENDING").ifPresent(transaction -> {
            if ("APPROVED".equals(decision)) {
                transaction.updateStatusApproved();
                System.out.println("Transaction status updated to COMPLETED for txId: " + txId);

                try {
                    PaymentDto payment = objectMapper.readValue(paymentJson, PaymentDto.class);
                    publishFinalizedEvent(payment, "APPROVED");
                    String userLocationKey = "user:" + payment.getUserId() + ":location";
                    redisTemplate.opsForValue().set(userLocationKey, payment.getCountry(), 24, TimeUnit.HOURS);
                    System.out.println("User location updated after verification for: " + payment.getUserId());
                } catch (JsonProcessingException e) {
                    System.err.println("Failed to parse payment DTO from Redis: " + e.getMessage());
                }

            } else {
                transaction.updateStatusDeniedByUser();
                System.out.println("Transaction status updated to DENIED_BY_USER for txId: " + txId);
            }
        });
        redisTemplate.delete(pendingTxKey);
    }

    private void saveTransaction(PaymentDto payment, String status) {
        // ID를 사용하여 실제 엔티티를 조회하지 않고, 참조(프록시)만 가져옵니다.
        User userProxy = entityManager.getReference(User.class, payment.getUserId());
        Store storeProxy = entityManager.getReference(Store.class, payment.getStoreId());

        Transaction transaction = Transaction.builder()
                .transactionId(payment.getTransactionId())
                .user(userProxy) // User 객체 참조를 전달
                .store(storeProxy) // Store 객체 참조를 전달
                .amount(BigDecimal.valueOf(payment.getAmount()))
                .country(payment.getCountry())
                .status(status)
                .transactionAt(payment.getTransactionAt())
                .build();
        transactionRepository.save(transaction);
    }
    private void publishFinalizedEvent(PaymentDto payment, String status) {
        TransactionFinalizedDto finalizedDto = TransactionFinalizedDto.builder()
                .transactionId(payment.getTransactionId())
                .userId(payment.getUserId())
                .cardId(payment.getCardId())
                .amount(payment.getAmount())
                .storeId(payment.getStoreId())
                .storeName(payment.getStoreName())
                .country(payment.getCountry())
                .transactionAt(payment.getTransactionAt())
                .status(status)
                .build();
        kafkaTemplate.send("transaction-finalized", finalizedDto);
    }
}
