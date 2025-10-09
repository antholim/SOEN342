package model;

import java.time.Duration;
import java.time.LocalTime;

public class TimeUtils {
    public static long minutesBetween(LocalTime a, LocalTime b) {
        long m = Duration.between(a, b).toMinutes();
        return m >= 0 ? m : m + 24 * 60;
    }
}
