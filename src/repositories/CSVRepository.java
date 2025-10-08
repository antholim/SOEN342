package repositories;

import model.Record;
import parsers.DayParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;



public class CSVRepository {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private volatile static CSVRepository instance;
    private DayParser dayParser;

    private CSVRepository(DayParser dayParser) {
        this.dayParser = dayParser;
    }

    public static CSVRepository getInstance() {
        if (instance == null) {
            synchronized (CSVRepository.class) {
                if (instance == null) {
                    instance = new CSVRepository(DayParser.getInstance());
                }
            }
        }
        return instance;
    }
    public List<Record> getRoutes(String filePath) {
        List<Record> routes = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return routes;
            }

            String[] headers = headerLine.split(",");

            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
//                System.out.println(values[6].trim());
                Record route = new Record(
                        values[0].trim(),
                        values[1].trim(),
                        values[2].trim(),
                        LocalTime.parse(values[3].trim(), TIME_FMT),
                        values[4].trim().contains("(+1d)") ? LocalTime.parse(values[4].trim().replace(" (+1d)", ""), TIME_FMT) .plusHours(24) : LocalTime.parse(values[4].trim()), // check moi un fou one liner
                        values[5].trim(),
                        dayParser.parseDays(values[6].replaceAll("^\"|\"$", "")),
                        Double.parseDouble(values[7].trim()),
                        Double.parseDouble(values[8].trim()));

                routes.add(route);
            }
        } catch (IOException e) {
            System.out.println("Error reading CSV file: " + e.getMessage());
        }

        return routes;
    }
}
