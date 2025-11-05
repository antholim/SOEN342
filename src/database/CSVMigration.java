package database;

import model.Record;
import repositories.CSVRepository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;

public class CSVMigration {
    private static final String DB_URL = "jdbc:sqlite:train_system.db";
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public static void main(String[] args) {
        CSVMigration migration = new CSVMigration();
        
        try {
            migration.createTables();
            System.out.println("Tables created successfully!");
            
            migration.migrateRoutes("src/data/eu_rail_network.csv");
            System.out.println("Routes migrated successfully!");
            
            migration.migrateTrips("src/data/trips.csv");
            System.out.println("Trips migrated successfully!");
            
            System.out.println("\nMigration completed successfully!");
            migration.printStatistics();
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates all necessary database tables
     */
    public void createTables() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            // Drop existing tables if they exist
            stmt.execute("DROP TABLE IF EXISTS trips");
            stmt.execute("DROP TABLE IF EXISTS reservations");
            stmt.execute("DROP TABLE IF EXISTS travellers");
            stmt.execute("DROP TABLE IF EXISTS routes");
            
            // Create routes table
            stmt.execute("""
                CREATE TABLE routes (
                    route_id TEXT PRIMARY KEY,
                    departure_city TEXT NOT NULL,
                    arrival_city TEXT NOT NULL,
                    departure_time TEXT NOT NULL,
                    arrival_time TEXT NOT NULL,
                    train_type TEXT NOT NULL,
                    days_of_operation TEXT NOT NULL,
                    first_class_rate REAL NOT NULL,
                    second_class_rate REAL NOT NULL
                )
                """);
            
            // Create travellers table
            stmt.execute("""
                CREATE TABLE travellers (
                    traveller_id TEXT PRIMARY KEY,
                    first_name TEXT NOT NULL,
                    last_name TEXT NOT NULL,
                    age INTEGER NOT NULL
                )
                """);
            
            // Create trips table with auto-increment numeric ID
            stmt.execute("""
                CREATE TABLE trips (
                    trip_id INTEGER PRIMARY KEY AUTOINCREMENT
                )
                """);
            
            // Create reservations table (links trips, travellers, and routes)
            stmt.execute("""
                CREATE TABLE reservations (
                    reservation_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    trip_id INTEGER NOT NULL,
                    ticket_number INTEGER NOT NULL,
                    traveller_id TEXT NOT NULL,
                    route_id TEXT NOT NULL,
                    FOREIGN KEY (trip_id) REFERENCES trips(trip_id),
                    FOREIGN KEY (traveller_id) REFERENCES travellers(traveller_id),
                    FOREIGN KEY (route_id) REFERENCES routes(route_id),
                    UNIQUE(trip_id, traveller_id)
                )
                """);
            
            System.out.println("Database tables created successfully!");
        }
    }

    /**
     * Migrates route data from CSV to database
     */
    public void migrateRoutes(String filePath) throws SQLException {
        CSVRepository csvRepo = CSVRepository.getInstance();
        List<Record> routes = csvRepo.getRoutes(filePath);
        
        String insertSQL = """
            INSERT INTO routes (route_id, departure_city, arrival_city, departure_time, 
                              arrival_time, train_type, days_of_operation, first_class_rate, 
                              second_class_rate)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            
            conn.setAutoCommit(false); // Use transaction for better performance
            
            for (Record route : routes) {
                pstmt.setString(1, route.getRouteId());
                pstmt.setString(2, route.getDepartureCity());
                pstmt.setString(3, route.getArrivalCity());
                pstmt.setString(4, route.getDepartureTime().format(TIME_FMT));
                pstmt.setString(5, route.getArrivalTime().format(TIME_FMT));
                pstmt.setString(6, route.getTrainType());
                pstmt.setString(7, formatDaysOfOperation(route.getDaysOfOperation()));
                pstmt.setDouble(8, route.getFirstClassRate());
                pstmt.setDouble(9, route.getSecondClassRate());
                pstmt.addBatch();
            }
            
            pstmt.executeBatch();
            conn.commit();
            
            System.out.println("Migrated " + routes.size() + " routes");
        }
    }

    /**
     * Migrates trip and traveller data from CSV to database
     * Maps old alphanumeric trip IDs to new numeric IDs
     */
    public void migrateTrips(String filePath) throws SQLException {
        String insertTravellerSQL = """
            INSERT OR IGNORE INTO travellers (traveller_id, first_name, last_name, age)
            VALUES (?, ?, ?, ?)
            """;
        String insertReservationSQL = """
            INSERT INTO reservations (trip_id, ticket_number, traveller_id, route_id)
            VALUES (?, ?, ?, ?)
            """;
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement travellerStmt = conn.prepareStatement(insertTravellerSQL);
             PreparedStatement reservationStmt = conn.prepareStatement(insertReservationSQL);
             BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            
            conn.setAutoCommit(false);
            
            // Skip header
            reader.readLine();
            
            int travellerCount = 0;
            int reservationCount = 0;
            
            // Map old alphanumeric trip IDs to new numeric ones
            java.util.Map<String, Long> tripIdMap = new java.util.HashMap<>();
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                
                if (values.length < 14) continue; // Skip malformed lines
                
                String oldTripId = values[0].trim();
                long ticketNumber = Long.parseLong(values[1].trim());
                String firstName = values[2].trim();
                String lastName = values[3].trim();
                int age = Integer.parseInt(values[4].trim());
                String travellerId = values[5].trim();
                String departureCity = values[6].trim();
                String arrivalCity = values[7].trim();
                String departureTime = values[8].trim();
                String arrivalTime = values[9].trim();
                String trainType = values[10].trim();
                String daysOfOperation = values[11].replaceAll("^\"|\"$", "").trim();
                double firstClassRate = Double.parseDouble(values[12].trim());
                double secondClassRate = Double.parseDouble(values[13].trim());
                
                // Get or create numeric trip ID
                long numericTripId;
                if (tripIdMap.containsKey(oldTripId)) {
                    numericTripId = tripIdMap.get(oldTripId);
                } else {
                    // Create new trip with auto-increment ID
                    try (Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("INSERT INTO trips (trip_id) VALUES (NULL)");
                        try (ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                            if (rs.next()) {
                                numericTripId = rs.getLong(1);
                                tripIdMap.put(oldTripId, numericTripId);
                            } else {
                                continue;
                            }
                        }
                    }
                }
                
                // Insert traveller
                travellerStmt.setString(1, travellerId);
                travellerStmt.setString(2, firstName);
                travellerStmt.setString(3, lastName);
                travellerStmt.setInt(4, age);
                travellerStmt.addBatch();
                travellerCount++;
                
                // Find matching route
                String routeId = findMatchingRoute(conn, departureCity, arrivalCity, 
                                                  departureTime, arrivalTime, trainType,
                                                  daysOfOperation, firstClassRate, secondClassRate);
                
                if (routeId != null) {
                    // Insert reservation
                    reservationStmt.setLong(1, numericTripId);
                    reservationStmt.setLong(2, ticketNumber);
                    reservationStmt.setString(3, travellerId);
                    reservationStmt.setString(4, routeId);
                    reservationStmt.addBatch();
                    reservationCount++;
                } else {
                    System.err.println("Warning: No matching route found for trip " + oldTripId);
                }
            }
            
            travellerStmt.executeBatch();
            reservationStmt.executeBatch();
            conn.commit();
            
            System.out.println("Migrated " + tripIdMap.size() + " trips (converted to numeric IDs)");
            System.out.println("Migrated " + travellerCount + " travellers (unique count may be lower)");
            System.out.println("Migrated " + reservationCount + " reservations");
            
        } catch (IOException e) {
            throw new SQLException("Error reading trips CSV file: " + e.getMessage(), e);
        }
    }

    /**
     * Finds a matching route ID based on trip details
     */
    private String findMatchingRoute(Connection conn, String departureCity, String arrivalCity,
                                     String departureTime, String arrivalTime, String trainType,
                                     String daysOfOperation, double firstClassRate, double secondClassRate) 
            throws SQLException {
        
        String query = """
            SELECT route_id FROM routes 
            WHERE departure_city = ? 
            AND arrival_city = ? 
            AND departure_time = ?
            AND train_type = ?
            AND first_class_rate = ?
            AND second_class_rate = ?
            """;
        
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, departureCity);
            pstmt.setString(2, arrivalCity);
            pstmt.setString(3, departureTime);
            pstmt.setString(4, trainType);
            pstmt.setDouble(5, firstClassRate);
            pstmt.setDouble(6, secondClassRate);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("route_id");
                }
            }
        }
        
        return null;
    }

    /**
     * Formats days of operation for storage
     */
    private String formatDaysOfOperation(HashSet<DayOfWeek> days) {
        if (days.size() == 7) {
            return "Daily";
        }
        
        StringBuilder sb = new StringBuilder();
        for (DayOfWeek day : DayOfWeek.values()) {
            if (days.contains(day)) {
                if (sb.length() > 0) sb.append(",");
                sb.append(formatDay(day));
            }
        }
        return sb.toString();
    }

    /**
     * Formats a single day
     */
    private String formatDay(DayOfWeek day) {
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

    /**
     * Prints statistics about the migrated data
     */
    public void printStatistics() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            System.out.println("\n=== Database Statistics ===");
            
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM routes");
            if (rs.next()) {
                System.out.println("Total routes: " + rs.getInt("count"));
            }
            
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM travellers");
            if (rs.next()) {
                System.out.println("Total travellers: " + rs.getInt("count"));
            }
            
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM trips");
            if (rs.next()) {
                System.out.println("Total trips: " + rs.getInt("count"));
            }
            
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM reservations");
            if (rs.next()) {
                System.out.println("Total reservations: " + rs.getInt("count"));
            }
            
            System.out.println("========================\n");
        }
    }
}
