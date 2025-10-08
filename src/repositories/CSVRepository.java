package repositories;

import enums.Records;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CSVRepository {
    private volatile static CSVRepository instance;
    private CSVRepository() {}

    public static CSVRepository getInstance() {
        if (instance == null) {
            synchronized (CSVRepository.class) {
                if (instance == null) {
                    instance = new CSVRepository();
                }
            }
        }
        return instance;
    }
    public List<Map<Records, String>> getRoutes(String filePath) {
        List<Map<Records, String>> routes = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return routes;
            }

            String[] headers = headerLine.split(",");

            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                Map<Records, String> route = new HashMap<>();

                for (int i = 0; i < headers.length; i++) {
                    System.out.println(headers[i].trim());
                    if (headers[i].trim().contains("First Class ticket rate (in euro)")) { //tech debt, we should find another way to doing that - lim
                        route.put(Records.FIRST_CLASS_TICKET_RATE, values[i].trim());
                    } else if (headers[i].trim().contains("Second Class ticket rate (in euro)")) {
                        route.put(Records.SECOND_CLASS_TICKET_RATE, values[i].trim());
                    } else {
                        route.put(Records.valueOf(headers[i].trim().toUpperCase().replace(" ", "_")), values[i].trim());
                    }
                }

                routes.add(route);
            }
        } catch (IOException e) {
            System.out.println("Error reading CSV file: " + e.getMessage());
        }

        return routes;
    }
}
