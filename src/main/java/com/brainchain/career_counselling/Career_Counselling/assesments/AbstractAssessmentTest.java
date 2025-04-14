package com.brainchain.career_counselling.Career_Counselling.assesments;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public abstract class AbstractAssessmentTest<T> implements CareerAssessmentTest<T> {
    protected Resource getResource(String filename) {
        return new ClassPathResource(filename);
    }
}