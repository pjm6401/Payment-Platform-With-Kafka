package com.project.fdsconsumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.common.PaymentDto;
import com.project.fdsconsumer.domain.Transaction;
import com.project.fdsconsumer.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class FdsService {

    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

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
            String txId = payment.getTransactionId();
            String pendingTxKey = "fds:pending:" + txId;
            try {
                String paymentJson = objectMapper.writeValueAsString(payment);
                redisTemplate.opsForValue().set(pendingTxKey, paymentJson, 5, TimeUnit.MINUTES);
            } catch (JsonProcessingException e) {
                return;
            }

            String verificationLink = "http://[EC2_PUBLIC_IP]:8083/verify/" + txId;
            String smsMessage = String.format("[국외결제] %s에서 %d원 결제 요청. 확인 링크: %s",
                    payment.getCountry(), payment.getAmount(), verificationLink);
            System.out.println("Sent SMS: " + smsMessage);
        } else {
            redisTemplate.opsForValue().set(userLocationKey, payment.getCountry(), 24, TimeUnit.HOURS);
            System.out.println("Normal transaction for user: " + userId);
        }
    }

    public void finalizeTransaction(String txId, String decision) {
        String pendingTxKey = "fds:pending:" + txId;
        String paymentJson = redisTemplate.opsForValue().get(pendingTxKey);
        if (paymentJson == null) return;

        if ("APPROVED".equals(decision)) {
            System.out.println("Transaction APPROVED by user: " + txId);
        } else {
            System.out.println("Transaction DENIED by user: " + txId);
        }
        redisTemplate.delete(pendingTxKey);
    }
}
