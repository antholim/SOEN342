import java.time.DayOfWeek;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import model.Record;
import model.Connection;
import model.TimeUtils;
import repositories.CSVRepository;
import service.ConnectionFinder;
import service.ConnectionSorter;

public class Main {
    public static CSVRepository repository = CSVRepository.getInstance();
    public static List<Record> listOfRoutes;

    public static void main(String[] args) {
        bootstrap();

        // You can comment this out if you don't want all routes printed
        // listOfRoutes.forEach(System.out::println);

        Scanner sc = new Scanner(System.in);

        System.out.println("\n=== Connection Finder ===");
        System.out.print("Departure city: ");
        String origin = sc.nextLine().trim();

        System.out.print("Arrival city: ");
        String destination = sc.nextLine().trim();

        System.out.print("Min transfer minutes (default 10): ");
        String minTxIn = sc.nextLine().trim();
        int minTransfer = minTxIn.isBlank() ? 10 : safeInt(minTxIn, 10);

        System.out.print("Max Connections (0=direct, 1=one stop, 2=two stops, default 2): ");
        String maxLegsIn = sc.nextLine().trim();
        int maxConnections = maxLegsIn.isBlank() ? 2 : safeInt(maxLegsIn, 2);
        int maxLegs = maxConnections + 1; // Convert connections to legs

        // Build connections
        ConnectionFinder finder = new ConnectionFinder(listOfRoutes);
        var connections = finder.findConnections(origin, destination, minTransfer, maxLegs);

        if (connections.isEmpty()) {
            System.out.println("\nNo connections found between " + origin + " and " + destination + ".");
            return;
        }

        // Sorting menu
        System.out.println("\nSort results by:");
        System.out.println("  0) No sorting");
        System.out.println("  1) Total duration");
        System.out.println("  2) Total price — 1st class");
        System.out.println("  3) Total price — 2nd class");
        System.out.print("Choose [0-3]: ");
        String choice = sc.nextLine().trim();

        switch (choice) {
            case "1" -> ConnectionSorter.sortByDuration(connections);
            case "2" -> ConnectionSorter.sortByFirstClassPrice(connections);
            case "3" -> ConnectionSorter.sortBySecondClassPrice(connections);
            default -> {} // No sorting
        }

        System.out.println("\nTotal Connection(s): " + connections.size() + "\n");

        int idx = 1;
        for (var pathList : connections) {
            System.out.println("=== Route " + (idx++) + " ===");
            printPath(pathList);
            System.out.println();
        }
    }

    public static void bootstrap() {
        listOfRoutes = repository.getRoutes("src/data/eu_rail_network.csv");
    }

    private static int safeInt(String s, int fallback) {
        try { return Integer.parseInt(s); } catch (Exception e) { return fallback; }
    }

    private static void printPath(List<Connection> path) {
        if (path == null || path.isEmpty()) {
            System.out.println("(empty path)");
            return;
        }

        // Display valid days for the entire journey
        Set<DayOfWeek> validDays = ConnectionFinder.getValidDaysForPath(path);
        System.out.println("  Valid Days: " + formatDays(validDays));
        System.out.println();

        for (int i = 0; i < path.size(); i++) {
            Connection c = path.get(i);
            System.out.println("  " + c.toString());

            if (i < path.size() - 1) {
                Connection next = path.get(i + 1);
                long transfer = TimeUtils.minutesBetween(c.arrTime(), next.depTime());
                System.out.println("    • Transfer: " + transfer + " min");
            }
        }

        long totalMinutes = ConnectionFinder.calculateTotalDuration(path);
        double totalFirst  = ConnectionFinder.calculateFirstClassTotal(path);
        double totalSecond = ConnectionFinder.calculateSecondClassTotal(path);

        System.out.println("  ---------------------------------------");
        System.out.printf ("  Total duration: %d min%n", totalMinutes);
        System.out.printf ("  1st class total: €%.2f%n", totalFirst);
        System.out.printf ("  2nd class total: €%.2f%n", totalSecond);
    }

    /**
     * Formats a set of days for display
     */
    private static String formatDays(Set<DayOfWeek> days) {
        if (days.isEmpty()) {
            return "None (Invalid connection)";
        }
        if (days.size() == 7) {
            return "Daily";
        }

        return days.stream()
                .sorted()
                .map(Main::formatDay)
                .collect(Collectors.joining(", "));
    }

    private static String formatDay(DayOfWeek day) {
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
}