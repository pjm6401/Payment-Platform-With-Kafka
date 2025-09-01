package com.project.loyaltyconsumer.repository;

import com.project.loyaltyconsumer.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {}
