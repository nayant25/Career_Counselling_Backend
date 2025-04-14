package com.brainchain.career_counselling.Career_Counselling.util;

import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CsvLoaderUtils {

    /**
     * Reads a CSV file and returns a list of rows (each row is a String array).
     *
     * @param resource         Spring Resource pointing to the CSV file
     * @param expectedColumns  Minimum number of columns expected per row
     * @return List of valid CSV rows
     * @throws IOException if file can't be read
     */
    public static List<String[]> readCsv(Resource resource, int expectedColumns) throws IOException {
        List<String[]> result = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }

                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                if (data.length < expectedColumns) {
                    System.err.println("⛔ Skipping invalid line (Expected at least " + expectedColumns + " columns): " + Arrays.toString(data));
                    continue;
                }

                result.add(data);
            }
        }
        return result;
    }
}
