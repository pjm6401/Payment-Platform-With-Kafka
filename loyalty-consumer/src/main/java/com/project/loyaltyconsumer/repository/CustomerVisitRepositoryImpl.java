package com.project.loyaltyconsumer.repository;

import com.project.loyaltyconsumer.domain.QCustomerVisit;
import com.project.loyaltyconsumer.domain.QTransaction;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomerVisitRepositoryImpl implements CustomerVisitRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public long countApprovedVisits(String userId, String storeId) {
        QCustomerVisit customerVisit = QCustomerVisit.customerVisit;
        QTransaction transaction = QTransaction.transaction;

        String approvedStatus = "APPROVED";

        Long count = queryFactory
                .select(customerVisit.count())
                .from(customerVisit)
                .join(customerVisit.transaction, transaction)
                .where(
                        customerVisit.user.userId.eq(userId),
                        customerVisit.store.storeId.eq(storeId),
                        transaction.status.eq(approvedStatus)
                )
                .fetchOne();

        return count == null ? 0 : count;
    }
}