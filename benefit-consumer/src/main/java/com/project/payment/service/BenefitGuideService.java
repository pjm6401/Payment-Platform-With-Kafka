package com.project.payment.service;


import com.project.common.PaymentDto;
import com.project.common.TransactionFinalizedDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BenefitGuideService {
    private final RedisTemplate<String, String> redisTemplate;

    public void analyzePaymentBenefits(TransactionFinalizedDto finalizedDto) {
        String storeId = finalizedDto.getStoreId();
        String benefitKey = "benefit:store:" + storeId;
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        Map<String, String> storeBenefits = hashOps.entries(benefitKey);

        if (storeBenefits.isEmpty()) return;

        System.out.println("Benefit analysis complete for store: " + finalizedDto.getStoreName());
    }
}
