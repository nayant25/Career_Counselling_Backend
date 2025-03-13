package com.brainchain.career_counselling.Career_Counselling.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brainchain.career_counselling.Career_Counselling.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(String phoneNumber); 
}