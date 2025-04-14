package com.brainchain.career_counselling.Career_Counselling.repository;

import com.brainchain.career_counselling.Career_Counselling.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollegeRepository extends JpaRepository<College, Long> {
    List<College> findByCareerCareerNameIgnoreCase(String careerName);
}
