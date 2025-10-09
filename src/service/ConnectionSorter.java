package service;

import model.Connection;
import java.util.Comparator;
import java.util.List;


public class ConnectionSorter {

    //all ascending order

    public static void sortByDuration(List<List<Connection>> connectionPaths) {
        connectionPaths.sort(
                Comparator.comparingLong(ConnectionFinder::calculateTotalDuration)
                        .thenComparingDouble(ConnectionFinder::calculateSecondClassTotal)
        );
    }

    public static void sortByFirstClassPrice(List<List<Connection>> connectionPaths) {
        connectionPaths.sort(
                Comparator.comparingDouble(ConnectionFinder::calculateFirstClassTotal)
                        .thenComparingLong(ConnectionFinder::calculateTotalDuration)
        );
    }

    public static void sortBySecondClassPrice(List<List<Connection>> connectionPaths) {
        connectionPaths.sort(
                Comparator.comparingDouble(ConnectionFinder::calculateSecondClassTotal)
                        .thenComparingLong(ConnectionFinder::calculateTotalDuration)
        );
    }
}
