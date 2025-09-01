package com.project.loyaltyconsumer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stores")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store {

    @Id
    @Column(name = "store_id")
    private String storeId;

    @Column(name = "store_name", nullable = false)
    private String storeName;

    @Column(nullable = false, length = 3)
    private String country;
}