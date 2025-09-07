package com.project.verification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.common.PaymentDto;
import com.project.common.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class VerificationController {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @GetMapping("/verify/{transactionId}")
    public String showVerificationPage(@PathVariable String transactionId, Model model) {
        String key = "fds:pending:" + transactionId;
        // 1. String 타입으로 Redis에서 JSON 데이터를 가져옵니다.
        String paymentJson = redisTemplate.opsForValue().get(key);

        if (paymentJson == null) {
            return "error"; // 데이터가 없거나 만료된 경우
        }

        try {
            // 2. 가져온 JSON 문자열을 PaymentDto 객체로 변환합니다.
            PaymentDto payment = objectMapper.readValue(paymentJson, PaymentDto.class);

            // 3. DTO 객체에서 데이터를 꺼내 템플릿에 전달합니다.
            model.addAttribute("transactionId", transactionId);
            model.addAttribute("amount", payment.getAmount());
            model.addAttribute("country", payment.getCountry());
            model.addAttribute("storeName", payment.getStoreName());

        } catch (JsonProcessingException e) {
            System.err.println("Failed to parse Payment DTO from Redis: " + e.getMessage());
            return "error";
        }

        return "verify";
    }

    @GetMapping("/verify/{transactionId}/approve")
    @ResponseBody
    public ResponseDto approve(@PathVariable String transactionId) {
        publishDecision(transactionId, "APPROVED");
        return new ResponseDto(transactionId, "APPROVED");
    }

    @GetMapping("/verify/{transactionId}/deny")
    @ResponseBody
    public ResponseDto deny(@PathVariable String transactionId) {
        publishDecision(transactionId, "DENIED");
        return new ResponseDto(transactionId, "DENIED");
    }

    private void publishDecision(String transactionId, String decision) {
        Map<String, String> decisionMap = new HashMap<>();
        decisionMap.put("transactionId", transactionId);
        decisionMap.put("decision", decision);
        kafkaTemplate.send("fds-decision-stream", decisionMap);
    }
}