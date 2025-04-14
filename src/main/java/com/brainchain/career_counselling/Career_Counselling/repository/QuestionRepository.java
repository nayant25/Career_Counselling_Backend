package com.brainchain.career_counselling.Career_Counselling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.brainchain.career_counselling.Career_Counselling.entity.Question;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByTest_Id(Integer testId);
    Optional<Question> findByQuestionIdAndTest_Id(Long questionId, Integer testId);
    long countByTest_Id(Integer testId);
}
