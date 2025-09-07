package com.project.loyaltyconsumer.repository;

import com.project.loyaltyconsumer.domain.CustomerVisit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerVisitRepository extends JpaRepository<CustomerVisit, Long>, CustomerVisitRepositoryCustom {
}
