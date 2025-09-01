package com.project.loyaltyconsumer.repository;

import com.project.loyaltyconsumer.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, String> {}
