package com.project.fdsconsumer.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction {

    @Id
    @Column(name = "transaction_id")
    private String transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String country;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "transaction_at", nullable = false)
    private LocalDateTime transactionAt;

    public void updateStatusApproved() {
        this.status = "APPROVED";
    }
    public void updateStatusDeniedByUser() {
        this.status = "DENIED_BY_USER";
    }
}