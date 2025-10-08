package parsers;


import java.time.DayOfWeek;
import java.util.HashSet;

public class DayParser {
    private volatile static DayParser instance;
    private DayParser() {}

    public static DayParser getInstance() {
        if (instance == null) {
            synchronized (DayParser.class) {
                if (instance == null) {
                    instance = new DayParser();
                }
            }
        }
        return instance;
    }
    public HashSet<DayOfWeek> parseDays(String s) {
        String[] parts = s.split(",");
        HashSet<DayOfWeek> days= new HashSet<>();

    }
    private static DayOfWeek parseDOW(String s) {
        s = s.trim().toLowerCase();
        switch (s) {
            case "monday", "mon" -> {
                return DayOfWeek.MONDAY;
            }
            case "tuesday", "tue" -> {
                return DayOfWeek.TUESDAY;
            }
            case "wednesday", "wed" -> {
                return DayOfWeek.WEDNESDAY;
            }
            case "thursday", "thu" -> {
                return DayOfWeek.THURSDAY;
            }
            case "friday", "fri" -> {
                return DayOfWeek.FRIDAY;
            }
            case "saturday", "sat" -> {
                return DayOfWeek.SATURDAY;
            }
            case "sunday", "sun" -> {
                return DayOfWeek.SUNDAY;
            }
            default -> {
                return null;
            }
        }
    }
}
