package com.project.loyaltyconsumer.repository;

import com.project.loyaltyconsumer.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, String> {}