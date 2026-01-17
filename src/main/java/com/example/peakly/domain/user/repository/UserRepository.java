package com.example.peakly.domain.user.repository;

import com.example.peakly.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long> {
}
