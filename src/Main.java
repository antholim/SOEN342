import java.util.List;
import model.Records;
import service.Csv;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, world!");
        List<Records> routes = Csv.load();  // call your loader

        System.out.println("Loaded " + routes.size() + " records.\n");

        // Print the first few entries to verify it's working
        for (int i = 0; i < Math.min(5, routes.size()); i++) {
            Records r = routes.get(i);
            System.out.println(
                r.getRouteId() + " | " +
                r.getDepartureCity() + " → " + r.getArrivalCity() + " | " +
                r.getDepartureTime() + "–" + r.getArrivalTime() + " | " +
                r.getTrainType() + " | 1st: " + r.getFirstClassRate() + "€"
            );
        }
    }
}
