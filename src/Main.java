import java.util.List;
import java.util.Scanner;
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

        // You can comment this out if you don’t want all routes printed
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

        System.out.print("Max Overlays (default 5): ");
        String maxLegsIn = sc.nextLine().trim();
        int maxLegs = maxLegsIn.isBlank() ? 5 : safeInt(maxLegsIn, 5);

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


        // -----------------------------------------------------------------------
        /*
        List<Record> allRoutes = Csv.load();
        Search search = new Search(allRoutes);
        System.out.println("=== Advanced Connection Search ===");
        System.out.print("Departure city (or leave blank): ");
        String dep = sc.nextLine();
        System.out.print("Arrival city (or leave blank): ");
        String arr = sc.nextLine();
        System.out.print("Train type (or leave blank): ");
        String type = sc.nextLine();
        System.out.print("Day of operation (e.g. Mon, Fri, Daily) (or leave blank): ");
        String day = sc.nextLine();
        System.out.print("Max 2nd class price (or leave blank): ");
        String priceInput = sc.nextLine();
        Double maxPrice = priceInput.isEmpty() ? null : Double.parseDouble(priceInput);

        List<Record> results = search.searchAdvanced(dep, arr, type, day, maxPrice);

        if (results.isEmpty()) {
            System.out.println("\nNo routes found matching all given criteria.");
        } else {
            System.out.println("\nFound " + results.size() + " matching routes:\n");
            for (Record r : results) {
                System.out.println(
                        r.getDepartureCity() + " → " + r.getArrivalCity() +
                        " | " + r.getDepartureTime() + "–" + r.getArrivalTime() +
                        " | " + r.getTrainType() +
                        " | " + r.getDaysOfOperation() +
                        " | 1st: " + r.getFirstClassRate() + "€" +
                        " | 2nd: " + r.getSecondClassRate() + "€"
                );
            }
        }
        */
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
}
