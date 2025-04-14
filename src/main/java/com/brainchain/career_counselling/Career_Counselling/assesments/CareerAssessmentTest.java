package com.brainchain.career_counselling.Career_Counselling.assesments;

public interface CareerAssessmentTest<T> {
    void loadQuestions();
    T calculateResult();
}
