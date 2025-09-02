package com.project.loyaltyconsumer;

import com.project.common.PaymentDto;
import com.project.loyaltyconsumer.service.LoyaltyService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoyaltyConsumer {

    private final LoyaltyService loyaltyService;

    @KafkaListener(topics = "transaction-finalized", groupId = "loyalty-group", containerFactory = "finalizedDtoContainerFactory")
    public void consumePayment(PaymentDto payment) {
        loyaltyService.analyzeCustomerVisit(payment);
    }
}
