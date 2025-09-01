package com.project.fdsconsumer.repository;


import com.project.fdsconsumer.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    Optional<Transaction> findTopByUser_UserIdOrderByTransactionAtDesc(String userId);
}
