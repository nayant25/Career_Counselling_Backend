package com.brainchain.career_counselling.Career_Counselling.repository;

import com.brainchain.career_counselling.Career_Counselling.entity.CareerTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CareerTestRepository extends JpaRepository<CareerTest, Integer> {

    Optional<CareerTest> findByIdAndStatus(Integer id, CareerTest.Status status);

    List<CareerTest> findAllByStatus(CareerTest.Status status);

    Optional<CareerTest> findByTestName(String testName);
}
