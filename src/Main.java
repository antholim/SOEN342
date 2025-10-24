import java.util.List;
import java.util.Scanner;
import model.Record;
import model.Connection;
import model.TimeUtils;
import model.Trip;
import model.Traveller;
import repositories.CSVRepository;
import repositories.TripCSVRepository;
import repositories.ClientRepository;
import service.ConnectionFinder;
import service.ConnectionSorter;
import service.AdvancedSearch;
import service.BookingService;

public class Main {
    public static CSVRepository repository = CSVRepository.getInstance();
    public static List<Record> listOfRoutes;
    public static Scanner sc = new Scanner(System.in);
    public static TripCSVRepository tripRepo = new TripCSVRepository("src/data/trips.csv");
    public static ClientRepository clientRepo = new ClientRepository();
    public static BookingService booking = new BookingService(tripRepo, clientRepo);

    public static void main(String[] args) {
        bootstrap();

        boolean running = true;
        while (running) {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║   EU RAIL NETWORK SEARCH SYSTEM        ║");
            System.out.println("╚════════════════════════════════════════╝");

            System.out.println("1. Route Search");
            System.out.println("2. View My Trips");
            System.out.println("Enter choice (1-2), or any other key to exit: ");
            String value = sc.nextLine().trim();
            if (value.equals("1")) {
                performSearch();
            } else if (value.equals("2")) {
                viewMyTrips();
            } else {
                running = false;
            }
        }

        System.out.println("\nThank you for using EU Rail Network Search System!");
        sc.close();
    }

    private static void viewMyTrips() {
        System.out.println("\n=== View My Trips ===\n");
        
        System.out.print("Enter your last name: ");
        String lastName = sc.nextLine().trim();
        
        System.out.print("Enter your ID (passport/gov): ");
        String travellerId = sc.nextLine().trim();
        
        if (lastName.isEmpty() || travellerId.isEmpty()) {
            System.out.println("Both last name and ID are required.");
            return;
        }
        
        List<Trip> myTrips = tripRepo.findTripsByTraveller(lastName, travellerId);
        
        if (myTrips.isEmpty()) {
            System.out.println("\nNo trips found for " + lastName + " with ID " + travellerId + ".");
        } else {
            System.out.println("\n✓ Found " + myTrips.size() + " trip(s) for " + lastName + ":\n");
            for (Trip trip : myTrips) {
                System.out.println("╔═══════════════════════════════════════╗");
                System.out.println("║  Trip ID: " + trip.tripId() + "           ║");
                System.out.println("╚═══════════════════════════════════════╝");
                
                for (var r : trip.reservations()) {
                    Traveller t = r.traveller();
                    Connection c = r.connection();
                    
                    System.out.println("  Passenger: " + t.firstName() + " " + t.lastName() + 
                                     " (age " + t.age() + ", ID: " + t.id() + ")");
                    System.out.println("  Ticket #: " + r.ticket().number());
                    System.out.println("  Route: " + c.from() + " → " + c.to());
                    System.out.println("  Departure: " + c.depTime() + " | Arrival: " + c.arrTime());
                    System.out.println("  Train: " + c.trainType());
                    System.out.println("  Days: " + formatDaysOfWeek(c.daysOfOperation().toString()));
                    System.out.printf("  Price: 1st class €%.2f | 2nd class €%.2f%n", 
                                    c.firstRate(), c.secondRate());
                    System.out.println("  ─────────────────────────────────────────");
                }
                System.out.println();
            }
        }
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

        AdvancedSearch search = new AdvancedSearch(listOfRoutes);
        List<Record> directResults = search.searchAdvanced(
                departure, arrival, trainType, day,
                maxFirstPrice, maxSecondPrice,
                minDepInput, maxDepInput, maxDuration
        );

        if (!directResults.isEmpty()) {
            System.out.println("\n✓ Found " + directResults.size() + " direct route(s):\n");

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

            System.out.print("\nBook one of these direct routes? (y/n): ");
            String book = sc.nextLine().trim().toLowerCase();
            if (book.equals("y") || book.equals("yes")) {
                int choice = askIndex("Enter route # to book (1-" + directResults.size() + "): ", 1, directResults.size());
                Record selected = directResults.get(choice - 1);
                var travellers = collectTravellers();
                Trip trip = booking.bookDirect(travellers, selected);
                printBooking(trip, "Direct", selected.getDepartureCity() + " → " + selected.getArrivalCity());
                return;
            }
        } else {
            System.out.println("\nNo direct routes found matching your criteria.");

            if (departure.isEmpty() || arrival.isEmpty()) {
                System.out.println("Cannot search for connections without departure and arrival cities.");
                return;
            }

            System.out.println("🔍 Searching for multi-leg connections...\n");

            System.out.print("Min transfer minutes (default 10): ");
            String minTxIn = sc.nextLine().trim();
            int minTransfer = minTxIn.isBlank() ? 10 : safeInt(minTxIn, 10);

            System.out.print("Max connections (1 or 2, default 2): ");
            String maxLegsIn = sc.nextLine().trim();
            int maxLegs = maxLegsIn.isBlank() ? 2 : Math.min(Math.max(safeInt(maxLegsIn, 2), 1), 2);

            ConnectionFinder finder = new ConnectionFinder(listOfRoutes);
            var connections = finder.findConnections(
                    departure, arrival, minTransfer, maxLegs,
                    trainType, day, maxFirstPrice, maxSecondPrice,
                    minDepInput, maxDepInput, maxDuration
            );

            if (connections.isEmpty()) {
                System.out.println("\nNo connections found between " + departure + " and " + arrival + ".");
            } else {
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
                    default -> {}
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

                System.out.print("Book one of these connections? (y/n): ");
                String bookConn = sc.nextLine().trim().toLowerCase();
                if (bookConn.equals("y") || bookConn.equals("yes")) {
                    int which = askIndex("Enter route # to book (1-" + connections.size() + "): ", 1, connections.size());
                    var pathToBook = connections.get(which - 1);
                    var travellers = collectTravellers();
                    Trip trip = booking.bookConnection(travellers, pathToBook);
                    printBooking(trip, "Connection", pathToBook.get(0).from() + " → " + pathToBook.get(pathToBook.size() - 1).to());
                    return;
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
        
        System.out.println("Loading existing trips...");
        List<Trip> existingTrips = tripRepo.loadTrips();
        System.out.println("✓ Loaded " + existingTrips.size() + " trips.");
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

    private static int askIndex(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int v = Integer.parseInt(sc.nextLine().trim());
                if (v >= min && v <= max) return v;
            } catch (Exception ignored) {}
            System.out.println("Please enter a number between " + min + " and " + max + ".");
        }
    }

    private static java.util.List<Traveller> collectTravellers() {
        System.out.print("Number of travellers: ");
        int n = safeInt(sc.nextLine().trim(), 1);
        java.util.List<Traveller> list = new java.util.ArrayList<>();
        for (int i = 1; i <= n; i++) {
            System.out.println("Traveller " + i + ":");
            System.out.print("  First name: "); String fn = sc.nextLine().trim();
            System.out.print("  Last  name: "); String ln = sc.nextLine().trim();
            System.out.print("  Age: "); int age = safeInt(sc.nextLine().trim(), 25);
            System.out.print("  ID (passport/gov): "); String pid = sc.nextLine().trim();
            list.add(new Traveller(fn, ln, age, pid));
        }
        return list;
    }

    private static void printBooking(Trip trip, String kind, String route) {
        System.out.println("\n✓ Trip booked (" + kind + ")");
        System.out.println("Trip ID: " + trip.tripId());
        System.out.println("Route  : " + route);
        System.out.println("Reservations / Tickets:");
        for (var r : trip.reservations()) {
            System.out.printf("  - %s %s (age %d): Ticket #%d%n",
                    r.traveller().firstName(), r.traveller().lastName(),
                    r.traveller().age(), r.ticket().number());
        }
    }
}