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

    @KafkaListener(topics = "payment-stream", groupId = "loyalty-group", containerFactory = "paymentDtoContainerFactory")
    public void consumePayment(PaymentDto payment) {
        loyaltyService.analyzeCustomerVisit(payment);
    }
}
