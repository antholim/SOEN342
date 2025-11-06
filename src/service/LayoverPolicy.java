package service;

import model.Connection;

import java.time.LocalTime;
import java.util.List;

public final class LayoverPolicy {
    private final LocalTime DAY_START = LocalTime.of(6, 0);
    private final LocalTime DAY_END   = LocalTime.of(22, 0);
    private final int MAX_DAY_MINUTES = 120;
    private final int MAX_NIGHT_MINUTES = 30;
    private final int MIN_TRANSFER_MINUTES = 5;
    public static LayoverPolicy INSTANCE;

    public static LayoverPolicy getInstance() {
        if (INSTANCE == null) {
            synchronized (LayoverPolicy.class) {
                if (INSTANCE == null) {
                    INSTANCE = new LayoverPolicy();
                }
            }
        }

        return INSTANCE;
    }
    private LayoverPolicy() {}



    public boolean isTransferAllowed(LocalTime arrival, LocalTime departure) {
        int minutes = minutesBetweenWrapped(arrival, departure);
        if (minutes < MIN_TRANSFER_MINUTES) {
            return false;
        }

        boolean arrivalAfterHours = isAfterHours(arrival);
        boolean departureAfterHours = isAfterHours(departure);

        int limit;
        if (arrivalAfterHours || departureAfterHours) {
            limit = MAX_NIGHT_MINUTES;
        } else {
            limit = MAX_DAY_MINUTES;
        }
        return minutes <= limit;
    }

    public boolean isPathAllowed(List<Connection> path) {
        if (path == null || path.size() <= 1) {
            return true;
        }
        for (int i = 0; i < path.size() - 1; i++) {
            LocalTime arr = path.get(i).arrTime();
            LocalTime dep = path.get(i + 1).depTime();
            if (!isTransferAllowed(arr, dep)) {
                return false;
            }
        }
        return true;
    }

    private boolean isAfterHours(LocalTime t) {
        return t.isBefore(DAY_START) || !t.isBefore(DAY_END);
    }

    private int minutesBetweenWrapped(LocalTime a, LocalTime b) {
        int aMin = a.toSecondOfDay() / 60;
        int bMin = b.toSecondOfDay() / 60;
        int diff = bMin - aMin;
        if (diff < 0) {
            diff += 24 * 60;
        }
        return diff;
    }
}
