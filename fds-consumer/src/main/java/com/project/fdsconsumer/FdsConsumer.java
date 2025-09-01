package com.project.fdsconsumer;

import com.project.common.PaymentDto;
import com.project.fdsconsumer.service.FdsService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FdsConsumer {
    private final FdsService fdsService;

    @KafkaListener(topics = "payment-stream", groupId = "fds-group", containerFactory = "paymentDtoContainerFactory")
    public void consumePayment(PaymentDto payment) {
        fdsService.processTransaction(payment);
    }

    @KafkaListener(topics = "fds-decision-stream", groupId = "fds-group", containerFactory = "mapContainerFactory")
    public void consumeDecision(Map<String, String> decision) {
        fdsService.finalizeTransaction(decision.get("transactionId"), decision.get("decision"));
    }
}
