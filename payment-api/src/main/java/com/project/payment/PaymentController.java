package com.project.payment;

import com.project.common.PaymentDto;
import com.project.common.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final KafkaTemplate<String, PaymentDto> kafkaTemplate;
    private final StringRedisTemplate redisTemplate;

    @PostMapping("/api/payment")
    public ResponseDto processPayment(@RequestBody PaymentDto paymentDto) throws InterruptedException {
        Thread.sleep(200);

        paymentDto.setTransactionId(UUID.randomUUID().toString());
        paymentDto.setTransactionAt(LocalDateTime.now());
        kafkaTemplate.send("payment-stream", paymentDto);
        return new ResponseDto(paymentDto.getTransactionId(), "APPROVED");
    }

    @PostMapping("/api/location/{userId}")
    public String updateUserLocation(@PathVariable String userId, @RequestBody String country) {
        String key = "user:" + userId + ":location";
        redisTemplate.opsForValue().set(key, country);
        redisTemplate.expire(key, 24, TimeUnit.HOURS);
        return "User location updated.";
    }
}
