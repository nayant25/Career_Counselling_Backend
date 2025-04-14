package com.brainchain.career_counselling.Career_Counselling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RIASECResult {
    private Map<String, Integer> categoryScores;
    private String hollandCode;
    
    public void calculateHollandCode() {
        List<String> topCategories = categoryScores.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Integer>>comparingInt(e -> -e.getValue()))
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();
        hollandCode = String.join("", topCategories);
    }
}
