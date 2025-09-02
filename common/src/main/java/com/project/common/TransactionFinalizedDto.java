package com.project.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionFinalizedDto {
    private String transactionId;
    private String userId;
    private String cardId;
    private Long amount;
    private String storeId;
    private String storeName;
    private String country;
    private LocalDateTime transactionAt;
    private String status; // 최종 상태 포함
}
