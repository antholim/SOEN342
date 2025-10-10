package service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import model.Record;
import model.TimeUtils;

public class AdvancedSearch {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private List<Record> records;

    public AdvancedSearch(List<Record> records) {
        this.records = records;
    }

    public List<Record> searchAdvanced(
            String departure,
            String arrival,
            String trainType,
            String day,
            Double maxFirstClassPrice,
            Double maxSecondClassPrice,
            String minDepartureTime,
            String maxDepartureTime,
            Integer maxDuration)
    {
        List<Record> results = new ArrayList<>();

        // Parse time constraints if provided
        LocalTime minDepTime = null;
        LocalTime maxDepTime = null;

        if (minDepartureTime != null && !minDepartureTime.isEmpty()) {
            try {
                minDepTime = LocalTime.parse(minDepartureTime, TIME_FMT);
            } catch (Exception e) {
                System.out.println("⚠ Invalid min departure time format. Ignoring this filter.");
            }
        }

        if (maxDepartureTime != null && !maxDepartureTime.isEmpty()) {
            try {
                maxDepTime = LocalTime.parse(maxDepartureTime, TIME_FMT);
            } catch (Exception e) {
                System.out.println("⚠ Invalid max departure time format. Ignoring this filter.");
            }
        }

        // Parse day if provided
        DayOfWeek searchDay = null;
        if (day != null && !day.isEmpty() && !day.equalsIgnoreCase("Daily")) {
            searchDay = parseDayOfWeek(day);
        }

        for (Record r : records) {
            boolean match = true;

            // Filter by departure city
            if (departure != null && !departure.isEmpty() &&
                    !r.getDepartureCity().equalsIgnoreCase(departure)) {
                match = false;
            }

            // Filter by arrival city
            if (arrival != null && !arrival.isEmpty() &&
                    !r.getArrivalCity().equalsIgnoreCase(arrival)) {
                match = false;
            }

            // Filter by train type
            if (trainType != null && !trainType.isEmpty() &&
                    !r.getTrainType().equalsIgnoreCase(trainType)) {
                match = false;
            }

            // Filter by day of operation
            if (searchDay != null && !r.getDaysOfOperation().contains(searchDay)) {
                match = false;
            }

            // Filter by first class price
            if (maxFirstClassPrice != null &&
                    r.getFirstClassRate() > maxFirstClassPrice) {
                match = false;
            }

            // Filter by second class price
            if (maxSecondClassPrice != null &&
                    r.getSecondClassRate() > maxSecondClassPrice) {
                match = false;
            }

            // Filter by minimum departure time
            if (minDepTime != null && r.getDepartureTime().isBefore(minDepTime)) {
                match = false;
            }

            // Filter by maximum departure time
            if (maxDepTime != null && r.getDepartureTime().isAfter(maxDepTime)) {
                match = false;
            }

            // Filter by maximum duration
            if (maxDuration != null) {
                long duration = TimeUtils.minutesBetween(r.getDepartureTime(), r.getArrivalTime());
                if (duration > maxDuration) {
                    match = false;
                }
            }

            if (match) {
                results.add(r);
            }
        }

        return results;
    }

    private DayOfWeek parseDayOfWeek(String day) {
        String normalized = day.trim().toLowerCase();

        return switch (normalized) {
            case "monday", "mon" -> DayOfWeek.MONDAY;
            case "tuesday", "tue" -> DayOfWeek.TUESDAY;
            case "wednesday", "wed" -> DayOfWeek.WEDNESDAY;
            case "thursday", "thu" -> DayOfWeek.THURSDAY;
            case "friday", "fri" -> DayOfWeek.FRIDAY;
            case "saturday", "sat" -> DayOfWeek.SATURDAY;
            case "sunday", "sun" -> DayOfWeek.SUNDAY;
            default -> {
                System.out.println("⚠ Unrecognized day: " + day + ". Ignoring day filter.");
                yield null;
            }
        };
    }

    public static List<Record> sortResults(List<Record> results, String sortChoice) {
        List<Record> sorted = new ArrayList<>(results);

        switch (sortChoice) {
            case "1" -> // Sort by departure time
                    sorted.sort(Comparator.comparing(Record::getDepartureTime));
            case "2" -> // Sort by duration
                    sorted.sort(Comparator.comparingLong(r ->
                            TimeUtils.minutesBetween(r.getDepartureTime(), r.getArrivalTime())));
            case "3" -> // Sort by first class price
                    sorted.sort(Comparator.comparingDouble(Record::getFirstClassRate));
            case "4" -> // Sort by second class price
                    sorted.sort(Comparator.comparingDouble(Record::getSecondClassRate));
            default -> {} // No sorting
        }

        return sorted;
    }
}