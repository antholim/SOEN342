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
        HashSet<DayOfWeek> days = new HashSet<>();
        if (s.equals("Daily")) {
            for (DayOfWeek day : DayOfWeek.values()) {
                days.add(day);
            }
            return days;
        }
        String[] parts = s.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.contains("-")) {
                String[] range = part.split("-");
                DayOfWeek start = parseDOW(range[0]);
                DayOfWeek end = parseDOW(range[1]);
                if (start != null && end != null) {
                    int startVal = start.getValue();
                    int endVal = end.getValue();
                    int i = startVal;
                    do {
                        days.add(DayOfWeek.of(i));
                        i = i % 7 + 1;
                    } while (i != endVal % 7 + 1);
                }
            } else {
                DayOfWeek day = parseDOW(part);
                if (day != null) days.add(day);
            }
        }
        return days;
    }

    private DayOfWeek parseDOW(String s) {
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
