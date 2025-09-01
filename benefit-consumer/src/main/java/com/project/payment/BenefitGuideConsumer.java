package com.project.payment;

import com.project.common.PaymentDto;
import com.project.payment.service.BenefitGuideService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BenefitGuideConsumer {
    private final BenefitGuideService benefitGuideService;

    @KafkaListener(topics = "payment-stream", groupId = "benefit-guide-group", containerFactory = "paymentDtoContainerFactory")
    public void consumePayment(PaymentDto payment) {
        benefitGuideService.analyzePaymentBenefits(payment);
    }
}
