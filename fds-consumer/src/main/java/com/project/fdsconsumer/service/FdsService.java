package com.project.fdsconsumer.service;

import com.project.common.PaymentDto;
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
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class FdsService {

    private final StringRedisTemplate redisTemplate;
    private final TransactionRepository transactionRepository;
    private final EntityManager entityManager; // EntityManager 주입

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
            HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
            hashOps.put(pendingTxKey, "amount", String.valueOf(payment.getAmount()));
            hashOps.put(pendingTxKey, "country", payment.getCountry());
            hashOps.put(pendingTxKey, "storeName", payment.getStoreName());
            redisTemplate.expire(pendingTxKey, 5, TimeUnit.MINUTES);
            String verificationLink = verificationApiUrl + "/verify/" + txId;

            String smsMessage = String.format("[국외결제] %s에서 %d원 결제 요청. 확인 링크: %s",
                    payment.getCountry(), payment.getAmount(), verificationLink);
            System.out.println("Sent SMS: " + smsMessage);
        } else {
            redisTemplate.opsForValue().set(userLocationKey, payment.getCountry(), 24, TimeUnit.HOURS);
            saveTransaction(payment, "APPROVED");
            System.out.println("Normal transaction for user: " + userId);
        }
    }

    @Transactional
    public void finalizeTransaction(String txId, String decision) {
        String pendingTxKey = "fds:pending:" + txId;
        if (Boolean.FALSE.equals(redisTemplate.hasKey(pendingTxKey))) return;

        // 3. 사용자 결정 후, DB의 거래 상태를 최종 UPDATE
        transactionRepository.findById(txId).ifPresent(transaction -> {
            String finalStatus = "APPROVED".equals(decision) ? "APPROVED" : "DENIED_BY_USER";
            if(finalStatus.equals("APPROVED")) {
                transaction.updateStatusApproved();
            }else{
                transaction.updateStatusDeniedByUser();
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
}
