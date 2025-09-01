package com.project.fdsconsumer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "home_country", nullable = false, length = 3)
    private String homeCountry;
}
