package com.brainchain.career_counselling.Career_Counselling.repository;

import com.brainchain.career_counselling.Career_Counselling.entity.Career;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CareerRepository extends JpaRepository<Career, Long> {
    Optional<Career> findByCareerNameIgnoreCase(String careerName);
}
