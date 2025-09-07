package com.project.loyaltyconsumer.repository;

public interface CustomerVisitRepositoryCustom {
    long countApprovedVisits(String userId, String storeId);
}
