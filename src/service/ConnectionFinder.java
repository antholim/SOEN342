package service;

import model.Connection;
import model.Record;
import model.TimeUtils;

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
                if (transferTime < minTransferMinutes) continue;
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
