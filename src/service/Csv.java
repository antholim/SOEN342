package service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import model.Record;

public class Csv {

    private static final String CSV_PATH = "eu_rail_network.csv";

    public static List<Record> load() {
        List<Record> recordsList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(CSV_PATH))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // ✅ Split by commas, but ignore commas inside quotes
                String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                // Validate column count
                if (fields.length < 9) {
                    System.err.println("Skipping invalid line: " + line);
                    continue;
                }

                try {
                    // Remove quotes around the Days of Operation field
                    String days = fields[6].trim().replace("\"", "");

                    Record record = new Record(
                            fields[0].trim(),
                            fields[1].trim(),
                            fields[2].trim(),
                            fields[3].trim(),
                            fields[4].trim(),
                            fields[5].trim(),
                            days,                            
                            Double.parseDouble(fields[7].trim()), 
                            Double.parseDouble(fields[8].trim())  
                    );

                    recordsList.add(record);

                } catch (NumberFormatException e) {
                    System.err.println("Invalid numeric data in line: " + line);
                }
            }

            System.out.println("CSV loaded successfully. Total records: " + recordsList.size());

        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }

        return recordsList;
    }
}
