package model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.stream.Collectors;

public class Connection {
    private final Record record;

    public Connection(Record record) { this.record = record; }
    public String from()      { return record.getDepartureCity(); }
    public String to()        { return record.getArrivalCity(); }
    public LocalTime depTime(){ return record.getDepartureTime(); }
    public LocalTime arrTime(){ return record.getArrivalTime(); }
    public double firstRate() { return record.getFirstClassRate(); }
    public double secondRate(){ return record.getSecondClassRate(); }
    public HashSet<DayOfWeek> daysOfOperation() { return record.getDaysOfOperation(); }
    public String trainType() { return record.getTrainType(); }

    /**
     * Formats days of operation for display
     */
    public String formatDaysOfOperation() {
        HashSet<DayOfWeek> days = daysOfOperation();

        if (days.size() == 7) {
            return "Daily";
        }

        // Convert to sorted list
        return days.stream()
                .sorted()
                .map(this::formatDay)
                .collect(Collectors.joining(", "));
    }

    private String formatDay(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "Mon";
            case TUESDAY -> "Tue";
            case WEDNESDAY -> "Wed";
            case THURSDAY -> "Thu";
            case FRIDAY -> "Fri";
            case SATURDAY -> "Sat";
            case SUNDAY -> "Sun";
        };
    }

    @Override
    public String toString() {
        return from() + " to " + to()
                + " | Departs: " + depTime()
                + " | Arrives: " + arrTime()
                + " | Train: " + trainType()
                + " | Operates: " + formatDaysOfOperation();
    }
}