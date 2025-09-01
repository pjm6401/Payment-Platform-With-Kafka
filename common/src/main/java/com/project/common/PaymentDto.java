package com.project.common;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private String transactionId;
    private String userId;
    private String cardId;
    private Long amount;
    private String storeId;
    private String storeName;
    private String country;
    private LocalDateTime transactionAt;
}
