package com.brainchain.career_counselling.Career_Counselling.assesments;

import java.io.InputStream;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.brainchain.career_counselling.Career_Counselling.dto.MITestQuestion;
import com.brainchain.career_counselling.Career_Counselling.dto.MITestResult;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

@Service
public class MultipleIntelligencesTest extends AbstractAssessmentTest<MITestResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultipleIntelligencesTest.class);
    private List<MITestQuestion> questions = new ArrayList<>();
    private final Map<Integer, Integer> userResponses = new HashMap<>();

    @Override
    public void loadQuestions() {
        try (InputStream is = getResource("Gartner_Multiple_Intelligences_Test.csv").getInputStream()) {
            CsvMapper mapper = new CsvMapper();
            CsvSchema schema = CsvSchema.emptySchema().withHeader();
            MappingIterator<MITestQuestion> it = mapper.readerFor(MITestQuestion.class).with(schema).readValues(is);
            questions = new ArrayList<>();
            int skipped = 0;
            for (MITestQuestion q : it.readAll()) {
                if (isValid(q)) questions.add(q);
                else skipped++;
            }
            LOGGER.info("✅ Loaded {} MI questions, Skipped: {}", questions.size(), skipped);
        } catch (Exception ex) {
            LOGGER.error("⛔ Error loading MI test questions", ex);
            throw new RuntimeException("Error loading MI test questions", ex);
        }
    }

    private boolean isValid(MITestQuestion q) {
        return q.getQuestionId() > 0 &&
               q.getIntelligenceType() != null &&
               !q.getIntelligenceType().isBlank();
    }

    @Override
    public MITestResult calculateResult() {
        Map<String, Integer> scores = new HashMap<>();

        for (MITestQuestion q : questions) {
            int rating = userResponses.getOrDefault(q.getQuestionId(), 3); // Neutral if not answered
            String intelligence = q.getIntelligenceType().trim();
            scores.merge(intelligence, rating, Integer::sum);
        }

        MITestResult result = new MITestResult();
        result.setIntelligenceScores(scores);
        result.calculateDominantIntelligences();

        LOGGER.info("🧠 MI Result: Scores = {}, Dominant = {}", scores, result.getDominantIntelligences());
        return result;
    }

    public void setUserResponse(int questionId, int rating) {
        userResponses.put(questionId, rating);
    }
}