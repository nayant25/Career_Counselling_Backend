package com.brainchain.career_counselling.Career_Counselling.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "question")
@Data
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private CareerTest test;

    @NotBlank(message = "Question text is required")
    @Column(name = "question", nullable = false)
    private String question;

    @NotBlank(message = "Option A is required")
    @Column(name = "option_a", nullable = false)
    private String optionA;

    @NotBlank(message = "Option B is required")
    @Column(name = "option_b", nullable = false)
    private String optionB;

    @Column(name = "option_c")
    private String optionC;

    @Column(name = "option_d")
    private String optionD;

    @Column(name = "correct_answer")
    private String correctAnswer;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    private List<UserResponse> userResponses;

    @PrePersist
    @PreUpdate
    public void validateCorrectAnswer() {
        try {
            // Only validate if this test requires it
            if (test != null && Boolean.TRUE.equals(test.isValidateAnswer())) {
                String correct = correctAnswer == null ? "" : correctAnswer.trim().toUpperCase();
                boolean isValid = switch (correct) {
                    case "A" -> optionA != null && !optionA.trim().isEmpty();
                    case "B" -> optionB != null && !optionB.trim().isEmpty();
                    case "C" -> optionC != null && !optionC.trim().isEmpty();
                    case "D" -> optionD != null && !optionD.trim().isEmpty();
                    default -> false;
                };

                if (!isValid) {
                    throw new IllegalArgumentException(
                        "Correct answer must be A/B/C/D and point to a non-empty option. Found: " + correctAnswer
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Warning during correctAnswer validation: " + e.getMessage());
            
        }
    }
}
