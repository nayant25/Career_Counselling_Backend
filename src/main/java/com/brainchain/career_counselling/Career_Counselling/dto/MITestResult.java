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
public class MITestResult {
    private Map<String, Integer> intelligenceScores;
    private List<String> dominantIntelligences;
    
    public void calculateDominantIntelligences() {
        dominantIntelligences = intelligenceScores.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Integer>>comparingInt(e -> -e.getValue()))
                .limit(2)
                .map(Map.Entry::getKey)
                .toList();
    }
}
