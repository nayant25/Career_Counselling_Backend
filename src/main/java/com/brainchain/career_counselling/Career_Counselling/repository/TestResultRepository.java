package com.brainchain.career_counselling.Career_Counselling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.brainchain.career_counselling.Career_Counselling.entity.TestResult;
import java.util.List;
import java.util.Optional;

@Repository
public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    Optional<TestResult> findByUserIdAndTest_Id(Long userId, Integer testId);
    List<TestResult> findByUserId(Long userId);
    List<TestResult> findByTest_Id(Integer testId);
    long countByUserId(Long userId);
}
