import enums.Records;
import repositories.CSVRepository;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static CSVRepository repository = CSVRepository.getInstance();
    public static List<Map<Records, String>> listOfRoutes;
    public static void main(String[] args) {
        bootstrap(); //init
        for (Map<Records, String> route : listOfRoutes) {
            System.out.println(route);
        }

        Scanner sc = new Scanner(System.in);

    }

    public static void bootstrap() {
        listOfRoutes =  repository.getRoutes("src/data/eu_rail_network.csv");
    }
}
