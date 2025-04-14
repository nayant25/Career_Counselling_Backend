package com.brainchain.career_counselling.Career_Counselling.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.brainchain.career_counselling.Career_Counselling.entity.UserResponse;

@Repository
public interface UserResponseRepository extends JpaRepository<UserResponse, Long> {

    @Query("select ur from UserResponse ur where ur.user.id = :userId and ur.testResult.test.id = :testId")
    List<UserResponse> findByUserIdAndTestResultTestId(@Param("userId") Long userId, 
                                                       @Param("testId") Integer testId);

    @Query("select ur from UserResponse ur where ur.user.id = :userId and ur.testResult.test.id = :testId and ur.question.questionId = :questionId")
    Optional<UserResponse> findByUserIdAndTestResultTestIdAndQuestionQuestionId(@Param("userId") Long userId,
                                                                               @Param("testId") Integer testId,
                                                                               @Param("questionId") Long questionId);

    @Query("select count(ur) from UserResponse ur where ur.user.id = :userId and ur.testResult.test.id = :testId")
    long countByUserIdAndTestResultTestId(@Param("userId") Long userId, @Param("testId") Integer testId);

    @Query("select ur from UserResponse ur where ur.user.email = :email and ur.testResult.test.id = :testId")
    List<UserResponse> findByUserEmailAndTestResultTestId(@Param("email") String email, @Param("testId") Integer testId);
}
