package com.brainchain.career_counselling.Career_Counselling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching // Enable caching
public class CareerCounsellingApplication {
    public static void main(String[] args) {
        SpringApplication.run(CareerCounsellingApplication.class, args);
    }
}