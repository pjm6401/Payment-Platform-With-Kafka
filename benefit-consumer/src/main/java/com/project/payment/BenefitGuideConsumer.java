package com.project.payment;

import com.project.common.PaymentDto;
import com.project.common.TransactionFinalizedDto;
import com.project.payment.service.BenefitGuideService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BenefitGuideConsumer {
    private final BenefitGuideService benefitGuideService;

    @KafkaListener(topics = "transaction-finalized", groupId = "benefit-guide-group", containerFactory = "finalizedDtoContainerFactory")
    public void consumePayment(TransactionFinalizedDto finalizedDto) {
        benefitGuideService.analyzePaymentBenefits(finalizedDto);
    }
}
