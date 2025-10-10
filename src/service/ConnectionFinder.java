package service;

import model.Connection;
import model.Record;
import model.TimeUtils;

import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

public class ConnectionFinder {

    private final Map<String, List<Record>> routesByDepartureCity;

    public ConnectionFinder(List<Record> allRoutes) {
        this.routesByDepartureCity = allRoutes.stream()
                .collect(Collectors.groupingBy(r -> r.getDepartureCity().toLowerCase()));
    }

    public List<List<Connection>> findConnections(String origin,
                                                  String destination,
                                                  int minTransferMinutes,
                                                  int maxConnections) {

        List<List<Connection>> foundConnections = new ArrayList<>();
        Deque<Record> currentPath = new ArrayDeque<>();
        Set<String> visitedCities = new HashSet<>();

        exploreConnections(origin, destination, minTransferMinutes, maxConnections,
                visitedCities, currentPath, foundConnections);

        return foundConnections;
    }

    /**
     * Returns the valid days of the week for an entire connection path
     */
    public static Set<DayOfWeek> getValidDaysForPath(List<Connection> path) {
        if (path.isEmpty()) return new HashSet<>();

        // Start with all days from the first leg
        Set<DayOfWeek> validDays = new HashSet<>(path.get(0).daysOfOperation());

        // For each subsequent leg, filter to only compatible days
        for (int i = 1; i < path.size(); i++) {
            Connection prev = path.get(i - 1);
            Connection curr = path.get(i);
            long transferTime = TimeUtils.minutesBetween(prev.arrTime(), curr.depTime());
            boolean sameDay = transferTime < (24 * 60 - 60);

            Set<DayOfWeek> compatibleDays = new HashSet<>();

            if (sameDay) {
                // Same day: both must operate on the same day
                for (DayOfWeek day : validDays) {
                    if (curr.daysOfOperation().contains(day)) {
                        compatibleDays.add(day);
                    }
                }
            } else {
                // Next day: previous leg's day must connect to next leg's next day
                for (DayOfWeek day : validDays) {
                    DayOfWeek nextDay = day == DayOfWeek.SUNDAY ? DayOfWeek.MONDAY : day.plus(1);
                    if (curr.daysOfOperation().contains(nextDay)) {
                        compatibleDays.add(day);
                    }
                }
            }

            validDays = compatibleDays;
        }

        return validDays;
    }

    //dfs for all possible paths
    private void exploreConnections(String currentCity,
                                    String targetCity,
                                    int minTransferMinutes,
                                    int maxConnections,
                                    Set<String> visitedCities,
                                    Deque<Record> currentPath,
                                    List<List<Connection>> allConnections) {

        if (currentPath.size() >= maxConnections) return;
        visitedCities.add(currentCity.toLowerCase());

        for (Record nextRoute : routesByDepartureCity.getOrDefault(currentCity.toLowerCase(), List.of())) {
            String nextCity = nextRoute.getArrivalCity();

            if (visitedCities.contains(nextCity.toLowerCase())) continue;

            if (!currentPath.isEmpty()) {
                Record previousRoute = currentPath.peekLast();
                long transferTime = TimeUtils.minutesBetween(previousRoute.getArrivalTime(),
                        nextRoute.getDepartureTime());

                // Check minimum transfer time
                if (transferTime < minTransferMinutes) continue;

                // Check if days of operation are compatible
                if (!areCompatibleDays(previousRoute, nextRoute, transferTime)) {
                    continue;
                }
            }

            currentPath.addLast(nextRoute);

            if (nextCity.equalsIgnoreCase(targetCity)) {
                List<Connection> validConnection = currentPath.stream()
                        .map(Connection::new)
                        .collect(Collectors.toList());
                allConnections.add(validConnection);
            } else {

                exploreConnections(nextCity, targetCity, minTransferMinutes,
                        maxConnections, visitedCities, currentPath, allConnections);
            }

            currentPath.removeLast();
        }

        visitedCities.remove(currentCity.toLowerCase());
    }

    /**
     * Checks if two consecutive routes have compatible operating days.
     * Takes into account whether the connection happens on the same day or next day.
     */
    private boolean areCompatibleDays(Record previousRoute, Record nextRoute, long transferMinutes) {
        Set<DayOfWeek> prevDays = previousRoute.getDaysOfOperation();
        Set<DayOfWeek> nextDays = nextRoute.getDaysOfOperation();

        // Check if the transfer happens on the same day or crosses midnight
        boolean sameDay = transferMinutes < (24 * 60 - 60); // Less than ~23 hours means same day

        if (sameDay) {
            // For same-day connections, both routes must operate on at least one common day
            for (DayOfWeek day : prevDays) {
                if (nextDays.contains(day)) {
                    return true;
                }
            }
        } else {
            // For next-day connections, previous route day must connect to next route's next day
            for (DayOfWeek day : prevDays) {
                DayOfWeek nextDay = getNextDay(day);
                if (nextDays.contains(nextDay)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Gets the next day of the week
     */
    private DayOfWeek getNextDay(DayOfWeek day) {
        return day == DayOfWeek.SUNDAY ? DayOfWeek.MONDAY : day.plus(1);
    }

    //helper methods below
    public static long calculateTotalDuration(List<Connection> path) {
        if (path.isEmpty()) return 0;
        long totalMinutes = 0;

        for (int i = 0; i < path.size(); i++) {
            var leg = path.get(i);
            totalMinutes += TimeUtils.minutesBetween(leg.depTime(), leg.arrTime());

            if (i < path.size() - 1) {
                var nextLeg = path.get(i + 1);
                totalMinutes += TimeUtils.minutesBetween(leg.arrTime(), nextLeg.depTime());
            }
        }
        return totalMinutes;
    }

    public static double calculateFirstClassTotal(List<Connection> path) {
        return path.stream().mapToDouble(Connection::firstRate).sum();
    }

    public static double calculateSecondClassTotal(List<Connection> path) {
        return path.stream().mapToDouble(Connection::secondRate).sum();
    }
}