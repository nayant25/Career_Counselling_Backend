package com.brainchain.career_counselling.Career_Counselling.assesments;

import java.io.InputStream;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.brainchain.career_counselling.Career_Counselling.dto.*;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

@Service
public class IQTest extends AbstractAssessmentTest<IQTestResult> {
    private static final Logger LOGGER = LoggerFactory.getLogger(IQTest.class);
    private List<IQQuestion> questions = new ArrayList<>();
    private List<String> userResponses = new ArrayList<>();

    @Override
    public void loadQuestions() {
        try (InputStream is = getResource("IQ_Test_Questions.csv").getInputStream()) {
            CsvMapper mapper = new CsvMapper();
            CsvSchema schema = CsvSchema.emptySchema().withHeader();
            MappingIterator<IQQuestion> it = mapper.readerFor(IQQuestion.class).with(schema).readValues(is);
            questions = new ArrayList<>();
            int skipped = 0;
            for (IQQuestion q : it.readAll()) {
                if (isValidIQQuestion(q)) questions.add(q);
                else skipped++;
            }
            LOGGER.info("✅ Loaded {} IQ questions, Skipped: {}", questions.size(), skipped);
        } catch (Exception ex) {
            LOGGER.error("⛔ Error loading IQ test questions", ex);
            throw new RuntimeException("Error loading IQ test questions", ex);
        }
    }

    private boolean isValidIQQuestion(IQQuestion q) {
        String ans = q.getCorrectAnswer();
        if (ans == null) return false;
        ans = ans.trim().toUpperCase();
        return switch (ans) {
            case "A" -> q.getOptionA() != null && !q.getOptionA().isBlank();
            case "B" -> q.getOptionB() != null && !q.getOptionB().isBlank();
            case "C" -> q.getOptionC() != null && !q.getOptionC().isBlank();
            case "D" -> q.getOptionD() != null && !q.getOptionD().isBlank();
            default -> false;
        };
    }

    @Override
    public IQTestResult calculateResult() {
        if (questions.isEmpty()) throw new IllegalStateException("No IQ questions loaded.");

        int correctCount = 0;
        for (int i = 0; i < questions.size(); i++) {
            String response = (i < userResponses.size()) ? userResponses.get(i).trim().toUpperCase() : "";
            String correct = questions.get(i).getCorrectAnswer() != null ? questions.get(i).getCorrectAnswer().trim().toUpperCase() : "";
            if (!response.isEmpty() && response.equals(correct)) correctCount++;
        }

        IQTestResult result = new IQTestResult();
        result.setScore(correctCount);

        if (correctCount >= 27) {
            result.setCategory("Very High");
        } else if (correctCount >= 21) {
            result.setCategory("Above Average");
        } else if (correctCount >= 15) {
            result.setCategory("Average");
        } else {
            result.setCategory("Below Average");
        }


        LOGGER.info("IQ Test result: {} ({})", correctCount, result.getCategory());
        return result;
    }

    public void setUserResponses(List<String> responses) {
        userResponses.clear();
        if (responses != null) userResponses.addAll(responses);
    }
}