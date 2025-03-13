package com.brainchain.career_counselling.Career_Counselling.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String oauthProvider;
    private String oauthId;

    @Column(unique = true, nullable = false)
    private String phoneNumber;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean emailVerified = false;

    @Column(length = 6)
    private String emailOtp;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Role {
        ADMIN, STUDENT
    }

    public void setPassword(String password) {
        this.password = password != null ? new BCryptPasswordEncoder().encode(password) : null;
    }
}