package repositories;

import model.*;
import parsers.DayParser;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Database repository for managing trips
 * Replaces TripCSVRepository for database-based operations
 */
public class TripDatabaseRepository {
    private static final String DB_URL = "jdbc:sqlite:train_system.db";
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private final DayParser dayParser;

    public TripDatabaseRepository() {
        this.dayParser = DayParser.getInstance();
    }

    /**
     * Saves a trip to the database
     * Returns the generated numeric trip ID
     */
    public long saveTrip(Trip trip) {
        String insertTripSQL = "INSERT INTO trips (trip_id) VALUES (NULL)";
        String getLastIdSQL = "SELECT last_insert_rowid()";
        String insertReservationSQL = """
            INSERT INTO reservations (trip_id, ticket_number, traveller_id, route_id)
            VALUES (?, ?, ?, ?)
            """;

        try (java.sql.Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            long tripId;

            // Generate new trip ID
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(insertTripSQL);
                
                try (ResultSet rs = stmt.executeQuery(getLastIdSQL)) {
                    if (rs.next()) {
                        tripId = rs.getLong(1);
                    } else {
                        throw new SQLException("Failed to generate trip ID");
                    }
                }
            }

            // Insert reservations
            try (PreparedStatement pstmt = conn.prepareStatement(insertReservationSQL)) {
                for (Reservation reservation : trip.reservations()) {
                    Traveller traveller = reservation.traveller();
                    model.Connection connection = reservation.connection();
                    Ticket ticket = reservation.ticket();

                    // Save traveller first
                    saveTraveller(conn, traveller);

                    // Find matching route
                    String routeId = findMatchingRouteId(conn, connection);
                    
                    if (routeId != null) {
                        pstmt.setLong(1, tripId);
                        pstmt.setLong(2, ticket.number());
                        pstmt.setString(3, traveller.id());
                        pstmt.setString(4, routeId);
                        pstmt.addBatch();
                    } else {
                        System.err.println("Warning: No matching route found for reservation");
                    }
                }
                pstmt.executeBatch();
            }

            conn.commit();
            System.out.println("✓ Trip saved to database with ID: " + tripId);
            return tripId;

        } catch (SQLException e) {
            System.err.println("Error saving trip: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Loads all trips from the database
     */
    public List<Trip> loadTrips() {
        List<Trip> trips = new ArrayList<>();
        String query = "SELECT DISTINCT trip_id FROM trips ORDER BY trip_id";

        try (java.sql.Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                long tripId = rs.getLong("trip_id");
                Trip trip = loadTripById(tripId);
                if (trip != null && !trip.reservations().isEmpty()) {
                    trips.add(trip);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error loading trips: " + e.getMessage());
        }

        return trips;
    }

    /**
     * Loads a trip by its numerical ID
     */
    public Trip loadTripById(long tripId) {
        String query = """
            SELECT r.ticket_number, t.first_name, t.last_name, t.age, t.traveller_id,
                   rt.route_id, rt.departure_city, rt.arrival_city, rt.departure_time, 
                   rt.arrival_time, rt.train_type, rt.days_of_operation, 
                   rt.first_class_rate, rt.second_class_rate
            FROM reservations r
            JOIN travellers t ON r.traveller_id = t.traveller_id
            JOIN routes rt ON r.route_id = rt.route_id
            WHERE r.trip_id = ?
            """;

        try (java.sql.Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setLong(1, tripId);

            try (ResultSet rs = pstmt.executeQuery()) {
                Trip trip = new Trip(String.valueOf(tripId));

                while (rs.next()) {
                    // Parse traveller
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    int age = rs.getInt("age");
                    String travellerId = rs.getString("traveller_id");
                    Traveller traveller = new Traveller(firstName, lastName, age, travellerId);

                    // Parse route
                    String routeId = rs.getString("route_id");
                    String depCity = rs.getString("departure_city");
                    String arrCity = rs.getString("arrival_city");
                    LocalTime depTime = LocalTime.parse(rs.getString("departure_time"), TIME_FMT);
                    LocalTime arrTime = LocalTime.parse(rs.getString("arrival_time"), TIME_FMT);
                    String trainType = rs.getString("train_type");
                    HashSet<DayOfWeek> days = dayParser.parseDays(rs.getString("days_of_operation"));
                    double firstClass = rs.getDouble("first_class_rate");
                    double secondClass = rs.getDouble("second_class_rate");

                    model.Record record = new model.Record(routeId, depCity, arrCity, depTime, arrTime,
                            trainType, days, firstClass, secondClass);
                    model.Connection connection = new model.Connection(record);

                    // Parse ticket
                    long ticketNumber = rs.getLong("ticket_number");
                    Ticket ticket = new Ticket(ticketNumber);

                    // Create reservation
                    Reservation reservation = new Reservation(traveller, connection, ticket);
                    
                    try {
                        trip.addReservation(reservation);
                    } catch (IllegalArgumentException e) {
                        // Duplicate reservation, skip
                    }
                }

                return trip.reservations().isEmpty() ? null : trip;
            }

        } catch (SQLException e) {
            System.err.println("Error loading trip by ID: " + e.getMessage());
            return null;
        }
    }

    /**
     * Finds all trips for a specific traveller
     */
    public List<Trip> findTripsByTraveller(String lastName, String travellerId) {
        List<Trip> matchingTrips = new ArrayList<>();
        String query = """
            SELECT DISTINCT r.trip_id
            FROM reservations r
            JOIN travellers t ON r.traveller_id = t.traveller_id
            WHERE LOWER(t.last_name) = LOWER(?) AND t.traveller_id = ?
            ORDER BY r.trip_id
            """;

        try (java.sql.Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, lastName);
            pstmt.setString(2, travellerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    long tripId = rs.getLong("trip_id");
                    Trip trip = loadTripById(tripId);
                    if (trip != null) {
                        matchingTrips.add(trip);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error finding trips by traveller: " + e.getMessage());
        }

        return matchingTrips;
    }

    /**
     * Deletes a trip and all its reservations
     */
    public void deleteTrip(long tripId) {
        String deleteReservations = "DELETE FROM reservations WHERE trip_id = ?";
        String deleteTrip = "DELETE FROM trips WHERE trip_id = ?";

        try (java.sql.Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt1 = conn.prepareStatement(deleteReservations);
                 PreparedStatement pstmt2 = conn.prepareStatement(deleteTrip)) {

                pstmt1.setLong(1, tripId);
                pstmt1.executeUpdate();

                pstmt2.setLong(1, tripId);
                pstmt2.executeUpdate();

                conn.commit();
                System.out.println("✓ Trip " + tripId + " deleted");
            }

        } catch (SQLException e) {
            System.err.println("Error deleting trip: " + e.getMessage());
        }
    }

    /**
     * Gets the count of all trips
     */
    public int getTripCount() {
        String query = "SELECT COUNT(*) as count FROM trips";

        try (java.sql.Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt("count");
            }

        } catch (SQLException e) {
            System.err.println("Error getting trip count: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Saves a traveller to the database
     */
    private void saveTraveller(java.sql.Connection conn, Traveller traveller) throws SQLException {
        String insertSQL = """
            INSERT OR REPLACE INTO travellers (traveller_id, first_name, last_name, age)
            VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, traveller.id());
            pstmt.setString(2, traveller.firstName());
            pstmt.setString(3, traveller.lastName());
            pstmt.setInt(4, traveller.age());
            pstmt.executeUpdate();
        }
    }

    /**
     * Finds a matching route ID for a connection
     */
    private String findMatchingRouteId(java.sql.Connection conn, model.Connection connection) throws SQLException {
        String query = """
            SELECT route_id FROM routes
            WHERE departure_city = ?
            AND arrival_city = ?
            AND departure_time = ?
            AND train_type = ?
            AND first_class_rate = ?
            AND second_class_rate = ?
            LIMIT 1
            """;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, connection.from());
            pstmt.setString(2, connection.to());
            pstmt.setString(3, connection.depTime().format(TIME_FMT));
            pstmt.setString(4, connection.trainType());
            pstmt.setDouble(5, connection.firstRate());
            pstmt.setDouble(6, connection.secondRate());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("route_id");
                }
            }
        }

        return null;
    }
}
