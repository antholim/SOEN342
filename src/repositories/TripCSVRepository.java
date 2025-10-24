package repositories;

import model.*;
import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import parsers.DayParser;

public class TripCSVRepository {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private final String filePath;
    private final DayParser dayParser;

    public TripCSVRepository(String filePath) {
        this.filePath = filePath;
        this.dayParser = DayParser.getInstance();
    }

    /**
     * Saves a trip to the CSV file by appending it
     */
    public void saveTrip(Trip trip) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            for (Reservation reservation : trip.reservations()) {
                String line = formatReservationAsCSV(trip.tripId(), reservation);
                writer.write(line);
                writer.newLine();
            }
            System.out.println("✓ Trip saved to file: " + trip.tripId());
        } catch (IOException e) {
            System.out.println("Error writing trip to CSV: " + e.getMessage());
        }
    }

    /**
     * Loads all trips from the CSV file
     */
    public List<Trip> loadTrips() {
        List<Trip> trips = new ArrayList<>();
        
        // Create file if it doesn't exist
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
                // Write header
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                    writer.write("trip_id,ticket_number,first_name,last_name,age,traveller_id," +
                               "departure_city,arrival_city,departure_time,arrival_time," +
                               "train_type,days_of_operation,first_class_rate,second_class_rate");
                    writer.newLine();
                }
            } catch (IOException e) {
                System.out.println("Error creating trips file: " + e.getMessage());
                return trips;
            }
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return trips;
            }

            String line;
            Trip currentTrip = null;
            String currentTripId = null;

            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                
                String tripId = values[0].trim();
                long ticketNumber = Long.parseLong(values[1].trim());
                String firstName = values[2].trim();
                String lastName = values[3].trim();
                int age = Integer.parseInt(values[4].trim());
                String travellerId = values[5].trim();
                String depCity = values[6].trim();
                String arrCity = values[7].trim();
                LocalTime depTime = LocalTime.parse(values[8].trim(), TIME_FMT);
                LocalTime arrTime = LocalTime.parse(values[9].trim(), TIME_FMT);
                String trainType = values[10].trim();
                HashSet<java.time.DayOfWeek> days = dayParser.parseDays(values[11].replaceAll("^\"|\"$", ""));
                double firstClass = Double.parseDouble(values[12].trim());
                double secondClass = Double.parseDouble(values[13].trim());

                // Create or get trip
                if (currentTrip == null || !currentTripId.equals(tripId)) {
                    if (currentTrip != null) {
                        trips.add(currentTrip);
                    }
                    currentTrip = new Trip(tripId);
                    currentTripId = tripId;
                }

                // Create traveller
                Traveller traveller = new Traveller(firstName, lastName, age, travellerId);

                // Create record and connection - explicitly use model.Record
                model.Record record = new model.Record(
                    tripId + "-" + ticketNumber, 
                    depCity, arrCity, depTime, arrTime,
                    trainType, days, firstClass, secondClass
                );
                Connection connection = new Connection(record);

                // Create ticket and reservation
                Ticket ticket = new Ticket(ticketNumber);
                Reservation reservation = new Reservation(traveller, connection, ticket);

                try {
                    currentTrip.addReservation(reservation);
                } catch (IllegalArgumentException e) {
                    // Duplicate reservation, skip
                }
            }

            // Add last trip
            if (currentTrip != null) {
                trips.add(currentTrip);
            }

        } catch (IOException e) {
            System.out.println("Error reading trips CSV: " + e.getMessage());
        }

        return trips;
    }

    /**
     * Finds all trips for a traveller by last name and ID
     */
    public List<Trip> findTripsByTraveller(String lastName, String travellerId) {
        List<Trip> allTrips = loadTrips();
        List<Trip> matchingTrips = new ArrayList<>();

        for (Trip trip : allTrips) {
            boolean hasMatch = false;
            for (Reservation reservation : trip.reservations()) {
                Traveller t = reservation.traveller();
                if (t.lastName().equalsIgnoreCase(lastName) && t.id().equals(travellerId)) {
                    hasMatch = true;
                    break;
                }
            }
            if (hasMatch) {
                matchingTrips.add(trip);
            }
        }

        return matchingTrips;
    }

    /**
     * Formats a reservation as a CSV line
     */
    private String formatReservationAsCSV(String tripId, Reservation reservation) {
        Traveller t = reservation.traveller();
        Connection c = reservation.connection();
        Ticket ticket = reservation.ticket();

        String daysStr = formatDaysOfOperation(c.daysOfOperation());

        return String.format("%s,%d,%s,%s,%d,%s,%s,%s,%s,%s,%s,\"%s\",%.2f,%.2f",
                tripId,
                ticket.number(),
                t.firstName(),
                t.lastName(),
                t.age(),
                t.id(),
                c.from(),
                c.to(),
                c.depTime().format(TIME_FMT),
                c.arrTime().format(TIME_FMT),
                c.trainType(),
                daysStr,
                c.firstRate(),
                c.secondRate());
    }

    /**
     * Formats days of operation for CSV output
     */
    private String formatDaysOfOperation(HashSet<java.time.DayOfWeek> days) {
        if (days.size() == 7) {
            return "Daily";
        }

        return days.stream()
                .sorted()
                .map(this::formatDay)
                .collect(java.util.stream.Collectors.joining(","));
    }

    private String formatDay(java.time.DayOfWeek day) {
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