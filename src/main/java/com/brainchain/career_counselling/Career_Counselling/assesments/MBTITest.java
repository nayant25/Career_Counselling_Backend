package com.brainchain.career_counselling.Career_Counselling.assesments;

import java.io.InputStream;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.brainchain.career_counselling.Career_Counselling.dto.MBTIQuestion;
import com.brainchain.career_counselling.Career_Counselling.dto.MBTITestResult;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

@Service
public class MBTITest extends AbstractAssessmentTest<MBTITestResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MBTITest.class);
    private List<MBTIQuestion> questions = new ArrayList<>();
    private final Map<Integer, Integer> userResponses = new HashMap<>();

    @Override
    public void loadQuestions() {
        try (InputStream is = getResource("youth_friendly_mbti_test.csv").getInputStream()) {
            CsvMapper mapper = new CsvMapper();
            CsvSchema schema = CsvSchema.emptySchema().withHeader();
            MappingIterator<MBTIQuestion> it = mapper.readerFor(MBTIQuestion.class).with(schema).readValues(is);
            questions = new ArrayList<>();
            int skipped = 0;
            for (MBTIQuestion q : it.readAll()) {
                if (isValid(q)) questions.add(q);
                else skipped++;
            }
            LOGGER.info("✅ Loaded {} MBTI questions, Skipped: {}", questions.size(), skipped);
        } catch (Exception ex) {
            LOGGER.error("⛔ Error loading MBTI questions", ex);
            throw new RuntimeException("Error loading MBTI test questions", ex);
        }
    }

    private boolean isValid(MBTIQuestion q) {
        return q.getQuestionId() > 0 &&
               q.getTraitDirection() != null &&
               q.getTraitDirection().trim().matches("(?i)[EISNTFJP]");
    }

    @Override
    public MBTITestResult calculateResult() {
        Map<String, Integer> traitCounts = new HashMap<>(Map.of(
            "E", 0, "I", 0, "S", 0, "N", 0, "T", 0, "F", 0, "J", 0, "P", 0
        ));

        for (MBTIQuestion q : questions) {
            int rating = userResponses.getOrDefault(q.getQuestionId(), 3); // Default to neutral
            String trait = q.getTraitDirection().trim().toUpperCase();

            if (rating >= 4) {
                traitCounts.merge(trait, 1, Integer::sum);
            } else if (rating <= 2) {
                traitCounts.merge(getOppositeTrait(trait), 1, Integer::sum);
            }
        }

        String mbtiType = "" +
            (traitCounts.get("E") >= traitCounts.get("I") ? "E" : "I") +
            (traitCounts.get("S") >= traitCounts.get("N") ? "S" : "N") +
            (traitCounts.get("T") >= traitCounts.get("F") ? "T" : "F") +
            (traitCounts.get("J") >= traitCounts.get("P") ? "J" : "P");

        MBTITestResult result = new MBTITestResult();
        result.setTraitCounts(traitCounts);
        result.setMbtiType(mbtiType);

        LOGGER.info("🧠 MBTI Test Result → Type: {}, Traits: {}", mbtiType, traitCounts);
        return result;
    }

    public void setUserResponse(int questionId, int rating) {
        userResponses.put(questionId, rating);
    }

    private String getOppositeTrait(String trait) {
        return switch (trait) {
            case "E" -> "I";
            case "I" -> "E";
            case "S" -> "N";
            case "N" -> "S";
            case "T" -> "F";
            case "F" -> "T";
            case "J" -> "P";
            case "P" -> "J";
            default -> "";
        };
    }
}