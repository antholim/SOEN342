import java.util.List;
import java.util.Scanner;
import model.Record;
import model.Connection;
import model.TimeUtils;
import repositories.CSVRepository;
import service.ConnectionFinder;
import service.ConnectionSorter;
import service.AdvancedSearch;

public class Main {
    public static CSVRepository repository = CSVRepository.getInstance();
    public static List<Record> listOfRoutes;
    public static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        bootstrap();

        boolean running = true;
        while (running) {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║   EU RAIL NETWORK SEARCH SYSTEM        ║");
            System.out.println("╚════════════════════════════════════════╝");

            performSearch();

            System.out.print("\nSearch again? (y/n): ");
            String again = sc.nextLine().trim().toLowerCase();
            running = again.equals("y") || again.equals("yes");
        }

        System.out.println("\nThank you for using EU Rail Network Search System!");
        sc.close();
    }

    private static void performSearch() {
        System.out.println("\n=== Route Search ===");
        System.out.println("(Leave blank to skip any filter)\n");

        System.out.print("Departure city: ");
        String departure = sc.nextLine().trim();

        System.out.print("Arrival city: ");
        String arrival = sc.nextLine().trim();

        System.out.print("Train type (e.g., TGV, ICE, AVE): ");
        String trainType = sc.nextLine().trim();

        System.out.print("Day of operation (Mon/Tue/Wed/Thu/Fri/Sat/Sun/Daily): ");
        String day = sc.nextLine().trim();

        System.out.print("Max 1st class price (€): ");
        String firstPriceInput = sc.nextLine().trim();
        Double maxFirstPrice = firstPriceInput.isEmpty() ? null : safeDouble(firstPriceInput);

        System.out.print("Max 2nd class price (€): ");
        String secondPriceInput = sc.nextLine().trim();
        Double maxSecondPrice = secondPriceInput.isEmpty() ? null : safeDouble(secondPriceInput);

        System.out.print("Min departure time (HH:MM, e.g., 09:00): ");
        String minDepInput = sc.nextLine().trim();

        System.out.print("Max departure time (HH:MM, e.g., 18:00): ");
        String maxDepInput = sc.nextLine().trim();

        System.out.print("Max duration (minutes): ");
        String maxDurInput = sc.nextLine().trim();
        Integer maxDuration = maxDurInput.isEmpty() ? null : safeInt(maxDurInput, null);

        // First try: Advanced search for direct routes
        AdvancedSearch search = new AdvancedSearch(listOfRoutes);
        List<Record> directResults = search.searchAdvanced(
                departure, arrival, trainType, day,
                maxFirstPrice, maxSecondPrice,
                minDepInput, maxDepInput, maxDuration
        );

        if (!directResults.isEmpty()) {
            // Found direct routes
            System.out.println("\n✓ Found " + directResults.size() + " direct route(s):\n");

            // Sort options
            System.out.println("Sort results by:");
            System.out.println("  0) No sorting");
            System.out.println("  1) Departure time");
            System.out.println("  2) Duration");
            System.out.println("  3) 1st class price");
            System.out.println("  4) 2nd class price");
            System.out.print("Choose [0-4]: ");
            String sortChoice = sc.nextLine().trim();

            directResults = AdvancedSearch.sortResults(directResults, sortChoice);

            displayDirectRoutes(directResults);
        } else {
            // No direct routes found - try multi-leg connections
            System.out.println("\n⚠ No direct routes found matching your criteria.");

            if (departure.isEmpty() || arrival.isEmpty()) {
                System.out.println("❌ Cannot search for connections without departure and arrival cities.");
                return;
            }

            System.out.println("🔍 Searching for multi-leg connections...\n");

            System.out.print("Min transfer minutes (default 10): ");
            String minTxIn = sc.nextLine().trim();
            int minTransfer = minTxIn.isBlank() ? 10 : safeInt(minTxIn, 10);

            System.out.print("Max connections (1 or 2, default 2): ");
            String maxLegsIn = sc.nextLine().trim();
            int maxLegs = maxLegsIn.isBlank() ? 2 : Math.min(Math.max(safeInt(maxLegsIn, 2), 1), 2);

            // Create ConnectionFinder with filters
            ConnectionFinder finder = new ConnectionFinder(listOfRoutes);
            var connections = finder.findConnections(
                    departure, arrival, minTransfer, maxLegs,
                    trainType, day, maxFirstPrice, maxSecondPrice,
                    minDepInput, maxDepInput, maxDuration
            );

            if (connections.isEmpty()) {
                System.out.println("\n❌ No connections found between " + departure + " and " + arrival + ".");
            } else {
                // Sorting menu
                System.out.println("\nSort results by:");
                System.out.println("  0) No sorting");
                System.out.println("  1) Total duration");
                System.out.println("  2) Total price — 1st class");
                System.out.println("  3) Total price — 2nd class");
                System.out.print("Choose [0-3]: ");
                String sortChoice = sc.nextLine().trim();

                switch (sortChoice) {
                    case "1" -> ConnectionSorter.sortByDuration(connections);
                    case "2" -> ConnectionSorter.sortByFirstClassPrice(connections);
                    case "3" -> ConnectionSorter.sortBySecondClassPrice(connections);
                    default -> {} // No sorting
                }

                System.out.println("\n✓ Total Connection(s): " + connections.size() + "\n");

                int idx = 1;
                for (var pathList : connections) {
                    System.out.println("╔═══════════════════════════════════════╗");
                    System.out.println("║  Route " + idx++ + "                  ║");
                    System.out.println("╚═══════════════════════════════════════╝");
                    printPath(pathList);
                    System.out.println();
                }
            }
        }
    }

    private static void displayDirectRoutes(List<Record> results) {
        System.out.println("\n" + "═".repeat(120));
        System.out.printf("%-15s %-15s %-10s %-10s %-8s %-15s %12s %12s %10s%n",
                "FROM", "TO", "DEPART", "ARRIVE", "DURATION", "TRAIN", "1ST CLASS", "2ND CLASS", "DAYS");
        System.out.println("═".repeat(120));

        for (Record r : results) {
            long duration = TimeUtils.minutesBetween(r.getDepartureTime(), r.getArrivalTime());
            String daysStr = formatDaysOfWeek(r.getDaysOfOperation().toString());

            System.out.printf("%-15s %-15s %-10s %-10s %6d min %-15s €%10.2f €%10.2f %-10s%n",
                    truncate(r.getDepartureCity(), 15),
                    truncate(r.getArrivalCity(), 15),
                    r.getDepartureTime(),
                    r.getArrivalTime(),
                    duration,
                    truncate(r.getTrainType(), 15),
                    r.getFirstClassRate(),
                    r.getSecondClassRate(),
                    truncate(daysStr, 10));
        }
        System.out.println("═".repeat(120));
    }

    public static void bootstrap() {
        System.out.println("Loading EU Rail Network data...");
        listOfRoutes = repository.getRoutes("src/data/eu_rail_network.csv");
        System.out.println("✓ Loaded " + listOfRoutes.size() + " routes.");
    }

    private static int safeInt(String s, Integer fallback) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return fallback != null ? fallback : 0;
        }
    }

    private static Double safeDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return null;
        }
    }

    private static String truncate(String s, int maxLen) {
        return s.length() > maxLen ? s.substring(0, maxLen - 2) + ".." : s;
    }

    private static String formatDaysOfWeek(String daysStr) {
        return daysStr
                .replaceAll("\\[|\\]", "")
                .replaceAll("MONDAY", "MON")
                .replaceAll("TUESDAY", "TUE")
                .replaceAll("WEDNESDAY", "WED")
                .replaceAll("THURSDAY", "THU")
                .replaceAll("FRIDAY", "FRI")
                .replaceAll("SATURDAY", "SAT")
                .replaceAll("SUNDAY", "SUN");
    }

    private static void printPath(List<Connection> path) {
        if (path == null || path.isEmpty()) {
            System.out.println("(empty path)");
            return;
        }

        for (int i = 0; i < path.size(); i++) {
            Connection c = path.get(i);
            long legDuration = TimeUtils.minutesBetween(c.depTime(), c.arrTime());

            System.out.println("  Leg " + (i + 1) + ": " + c.toString() +
                    " | Duration: " + legDuration + " min");

            if (i < path.size() - 1) {
                Connection next = path.get(i + 1);
                long transfer = TimeUtils.minutesBetween(c.arrTime(), next.depTime());
                System.out.println("    ⏱ Transfer in " + c.to() + ": " + transfer + " min");
            }
        }

        long totalMinutes = ConnectionFinder.calculateTotalDuration(path);
        double totalFirst  = ConnectionFinder.calculateFirstClassTotal(path);
        double totalSecond = ConnectionFinder.calculateSecondClassTotal(path);

        System.out.println("  ─────────────────────────────────────────");
        System.out.printf ("  Total duration: %d min (%.1f hours)%n",
                totalMinutes, totalMinutes / 60.0);
        System.out.printf ("  1st class total: €%.2f%n", totalFirst);
        System.out.printf ("  2nd class total: €%.2f%n", totalSecond);
    }
}