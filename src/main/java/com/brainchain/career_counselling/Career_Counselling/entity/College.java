package com.brainchain.career_counselling.Career_Counselling.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "college")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class College {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long collegeId;

    @Column(name = "college_name", nullable = false)
    private String collegeName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "career_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Career career;
}