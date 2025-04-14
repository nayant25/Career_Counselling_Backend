package com.brainchain.career_counselling.Career_Counselling.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "career")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Career {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long careerId;

    @Column(name = "career_name", nullable = false, unique = true)
    private String careerName;

    @Column(name = "iq_levels")
    private String iqLevels; // e.g., "Very High,High,Average"

    @Column(name = "mbti_types")
    private String mbtiTypes; // e.g., "INTJ,INFJ"

    @Column(name = "mi_tags")
    private String miTags; // e.g., "Linguistic,Logical-Mathematical"

    @Column(name = "riasec_codes")
    private String riasecCodes; // e.g., "Investigative,Realistic"

    @OneToMany(mappedBy = "career", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<College> colleges;
} 