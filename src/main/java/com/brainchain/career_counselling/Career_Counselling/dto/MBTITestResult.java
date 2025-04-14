package com.brainchain.career_counselling.Career_Counselling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MBTITestResult {
    private Map<String, Integer> traitCounts;
    private String mbtiType;
}

