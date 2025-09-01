package com.project.verification;

import com.project.common.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class VerificationController {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @GetMapping("/verify/{transactionId}/approve")
    public ResponseDto approve(@PathVariable String transactionId) {
        publishDecision(transactionId, "APPROVED");
        return new ResponseDto(transactionId, "APPROVED");
    }

    @GetMapping("/verify/{transactionId}/deny")
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
