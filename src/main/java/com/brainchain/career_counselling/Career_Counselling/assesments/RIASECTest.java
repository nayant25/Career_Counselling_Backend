package com.brainchain.career_counselling.Career_Counselling.assesments;

import java.io.InputStream;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.brainchain.career_counselling.Career_Counselling.dto.RIASECQuestion;
import com.brainchain.career_counselling.Career_Counselling.dto.RIASECResult;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

@Service
public class RIASECTest extends AbstractAssessmentTest<RIASECResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RIASECTest.class);
    private List<RIASECQuestion> questions = new ArrayList<>();
    private final Map<Integer, Integer> userResponses = new HashMap<>();

    @Override
    public void loadQuestions() {
        try (InputStream is = getResource("RIASEC_Interest_Inventory_Questions.csv").getInputStream()) {
            CsvMapper mapper = new CsvMapper();
            CsvSchema schema = CsvSchema.emptySchema().withHeader();
            MappingIterator<RIASECQuestion> it = mapper.readerFor(RIASECQuestion.class).with(schema).readValues(is);
            questions = new ArrayList<>();
            int skipped = 0;
            for (RIASECQuestion q : it.readAll()) {
                if (isValid(q)) questions.add(q);
                else skipped++;
            }
            LOGGER.info("✅ Loaded {} RIASEC questions, Skipped: {}", questions.size(), skipped);
        } catch (Exception ex) {
            LOGGER.error("⛔ Error loading RIASEC questions", ex);
            throw new RuntimeException("Error loading RIASEC test questions", ex);
        }
    }

    private boolean isValid(RIASECQuestion q) {
        return q.getQuestionId() > 0 &&
               q.getCategory() != null &&
               !q.getCategory().isBlank();
    }

    @Override
    public RIASECResult calculateResult() {
        Map<String, Integer> scores = new HashMap<>();

        for (RIASECQuestion q : questions) {
            int rating = userResponses.getOrDefault(q.getQuestionId(), 3); // Neutral score
            String category = q.getCategory().toUpperCase();
            scores.merge(category, rating, Integer::sum);
        }

        RIASECResult result = new RIASECResult();
        result.setCategoryScores(scores);
        result.calculateHollandCode();

        LOGGER.info("🎯 RIASEC Holland Code calculated: {}", result.getHollandCode());
        return result;
    }

    public void setUserResponse(int questionId, int rating) {
        userResponses.put(questionId, rating);
    }
}