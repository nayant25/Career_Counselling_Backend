package com.brainchain.career_counselling.Career_Counselling.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    @NotBlank(message = "Password is required")
    @Column(name = "password", nullable = false)
    private String password;
    @NotBlank(message = "Name is required")
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "phone_number")
    private String phoneNumber;
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.STUDENT;
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;
    @Column(name = "otp")
    private String otp;
    @Column(name = "reset_password_otp")
    private String resetPasswordOtp;
    @Column(name = "reset_password_expiry")
    private LocalDateTime resetPasswordExpiry;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<TestResult> testResults;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserResponse> userResponses;
    public enum Role {
        STUDENT, ADMIN
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
