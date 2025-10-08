package repositories;

import entities.Route;

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
    public List<Route> getRoutes(String filePath) {
        List<Route> routes = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return routes;
            }

            String[] headers = headerLine.split(",");

            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                Route route = new Route(
                        values[0].trim(),
                        values[1].trim(),
                        values[2].trim(),
                        LocalTime.parse(values[3].trim(), TIME_FMT),
                        LocalTime.parse(values[4].trim(), TIME_FMT),
                        values[5].trim(),
                        values[6].trim(),
                        Double.parseDouble(values[7].trim()),
                        Double.parseDouble(values[8].trim()));



//                for (int i = 0; i < headers.length; i++) {
//                    System.out.println(headers[i].trim());
//                    if (headers[i].trim().contains("First Class ticket rate (in euro)")) { //tech debt, we should find another way to doing that - lim
//                        route.put(Records.FIRST_CLASS_TICKET_RATE, values[i].trim());
//                    } else if (headers[i].trim().contains("Second Class ticket rate (in euro)")) {
//                        route.put(Records.SECOND_CLASS_TICKET_RATE, values[i].trim());
//                    } else {
//                        route.put(Records.valueOf(headers[i].trim().toUpperCase().replace(" ", "_")), values[i].trim());
//                    }
//                }

                routes.add(route);
            }
        } catch (IOException e) {
            System.out.println("Error reading CSV file: " + e.getMessage());
        }

        return routes;
    }
}
