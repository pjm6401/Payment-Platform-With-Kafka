package com.project.payment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class BenefitAdminController {
    private final RedisTemplate<String, String> redisTemplate;

    @PostMapping("/api/admin/benefits/{storeId}")
    public String addBenefits(@PathVariable String storeId, @RequestBody Map<String, String> benefits) {
        String key = "benefit:store:" + storeId;
        redisTemplate.opsForHash().putAll(key, benefits);
        // 실제로는 DB에도 저장하는 로직이 추가되어야 합니다.
        return "Benefits added for store " + storeId;
    }
}
