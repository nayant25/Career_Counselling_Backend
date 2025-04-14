package com.brainchain.career_counselling.Career_Counselling.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class AIService {

    @Value("${xai.api.key}")
    private String apiKey;

    @Value("${xai.api.url:https://api.x.ai/v1/completions}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AIService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String generateCareerSummary(int iqScore, List<String> recommendations) {
        if (iqScore < 0) {
            throw new IllegalArgumentException("IQ score cannot be negative");
        }
        List<String> safeRecommendations = recommendations != null ? recommendations : Collections.emptyList();
        if (safeRecommendations.isEmpty()) {
            throw new IllegalArgumentException("Career recommendations cannot be empty");
        }
        String prompt = String.format(
                "Given an IQ score of %d and career recommendations: %s, provide a concise summary of the user's strengths and suggested career paths.",
                iqScore, String.join(", ", safeRecommendations)
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(new RequestBody(prompt, 100));
        } catch (Exception e) {
            return getFallbackSummary(iqScore, safeRecommendations);
        }
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        try {
            String response = restTemplate.postForObject(apiUrl, entity, String.class);
            if (response == null) {
                return getFallbackSummary(iqScore, safeRecommendations);
            }
            JsonNode jsonResponse = objectMapper.readTree(response);
            String summary = jsonResponse.path("text").asText("AI summary unavailable");
            return summary.trim().isEmpty() ? getFallbackSummary(iqScore, safeRecommendations) : summary;
        } catch (RestClientException | IllegalArgumentException e) {
            return getFallbackSummary(iqScore, safeRecommendations);
        } catch (Exception e) {
            return "Error generating AI summary: " + e.getMessage();
        }
    }
    
    private String getFallbackSummary(int iqScore, List<String> recommendations) {
        return "Based on an IQ of " + iqScore + ", you have strengths suited for " + String.join(", ", recommendations) + ".";
    }
    
    private static class RequestBody {
        @SuppressWarnings("unused")
		public String prompt;
        @SuppressWarnings("unused")
		public int max_tokens;
        public RequestBody(String prompt, int maxTokens) {
            this.prompt = prompt;
            this.max_tokens = maxTokens;
        }
    }
}
