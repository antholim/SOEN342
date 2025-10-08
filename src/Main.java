import enums.Records;
import repositories.CSVRepository;

import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        CSVRepository repository = CSVRepository.getInstance();
        List<Map<Records, String>> listOfRoutes =  repository.getRoutes("src/data/eu_rail_network.csv");
        for (Map<Records, String> route : listOfRoutes) {
            System.out.println(route);
        }
    }
}
