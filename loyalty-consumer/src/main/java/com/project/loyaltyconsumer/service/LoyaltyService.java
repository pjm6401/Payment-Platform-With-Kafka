package com.project.loyaltyconsumer.service;

import com.project.common.PaymentDto;
import com.project.loyaltyconsumer.domain.CustomerVisit;
import com.project.loyaltyconsumer.domain.Store;
import com.project.loyaltyconsumer.domain.Transaction;
import com.project.loyaltyconsumer.domain.User;
import com.project.loyaltyconsumer.repository.CustomerVisitRepository;
import com.project.loyaltyconsumer.repository.StoreRepository;
import com.project.loyaltyconsumer.repository.TransactionRepository;
import com.project.loyaltyconsumer.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LoyaltyService {

    private final StringRedisTemplate redisTemplate;
    private final CustomerVisitRepository customerVisitRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public void analyzeCustomerVisit(PaymentDto payment) {
        String userId = payment.getUserId();
        String storeId = payment.getStoreId();
        String visitCountKey = "loyalty:customer:" + userId + ":store:" + storeId + ":visit_count";

        Long currentVisitCount = redisTemplate.opsForValue().increment(visitCountKey);

        if (currentVisitCount != null && currentVisitCount == 1) {
            long pastVisitCount = customerVisitRepository.countByUser_UserIdAndStore_StoreId(userId, storeId);
            if (pastVisitCount > 0) {
                currentVisitCount = pastVisitCount + 1;
                redisTemplate.opsForValue().set(visitCountKey, String.valueOf(currentVisitCount));
            }
        }

        redisTemplate.expire(visitCountKey, 90, TimeUnit.DAYS);
        saveVisitAsync(payment);

        if (currentVisitCount != null && currentVisitCount == 5) {
            System.out.println(String.format("[VIP EVENT] 고객 %s님이 %s 매장에 5번째 방문!", userId, payment.getStoreName()));
        }
    }

    @Async
    public void saveVisitAsync(PaymentDto payment) {
        User user = userRepository.findById(payment.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + payment.getUserId()));
        Store store = storeRepository.findById(payment.getStoreId())
                .orElseThrow(() -> new EntityNotFoundException("Store not found: " + payment.getStoreId()));
        Transaction transaction = transactionRepository.findById(payment.getTransactionId())
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found: " + payment.getTransactionId()));

        CustomerVisit customerVisit = CustomerVisit.builder()
                .user(user)
                .store(store)
                .transaction(transaction)
                .visitAt(payment.getTransactionAt())
                .build();

        customerVisitRepository.save(customerVisit);
    }
}