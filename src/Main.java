import java.util.List;
import java.util.Scanner;
import model.Record;
import service.Csv;
import service.Search;
import repositories.CSVRepository;
import java.util.Map;

public class Main {
    public static CSVRepository repository = CSVRepository.getInstance();
    public static List<Map<Record, String>> listOfRoutes;

    public static void main(String[] args) {
        List<Record> allRoutes = Csv.load();
        Search search = new Search(allRoutes);
        Scanner sc = new Scanner(System.in);

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

    }

    public static void bootstrap() {
        listOfRoutes =  repository.getRoutes("src/data/eu_rail_network.csv");
    }
}
