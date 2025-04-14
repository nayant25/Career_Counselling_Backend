package com.brainchain.career_counselling.Career_Counselling.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "career_test")
@Data
public class CareerTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_id")
    private Integer id;

    @NotBlank(message = "Test name is required")
    @Column(name = "test_name", nullable = false)
    private String testName;

    @Column(name = "description")
    private String description;

    // New property: if true, questions will validate that the correct answer matches one of the options.
    @Column(name = "validate_answer", nullable = false)
    private boolean validateAnswer;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions;

    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL)
    private List<TestResult> testResults;

    public enum Status {
        ACTIVE, INACTIVE
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
