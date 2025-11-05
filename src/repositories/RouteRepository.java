package repositories;

import model.Record;
import parsers.DayParser;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Database repository for managing routes (Records)
 * Replaces CSVRepository for database-based operations
 */
public class RouteRepository {
    private static final String DB_URL = "jdbc:sqlite:train_system.db";
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private volatile static RouteRepository instance;
    private final DayParser dayParser;

    private RouteRepository(DayParser dayParser) {
        this.dayParser = dayParser;
    }

    public static RouteRepository getInstance() {
        if (instance == null) {
            synchronized (RouteRepository.class) {
                if (instance == null) {
                    instance = new RouteRepository(DayParser.getInstance());
                }
            }
        }
        return instance;
    }

    /**
     * Loads all routes from the database
     */
    public List<Record> getRoutes() {
        List<Record> routes = new ArrayList<>();
        String query = "SELECT * FROM routes ORDER BY route_id";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                routes.add(mapResultSetToRecord(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error loading routes from database: " + e.getMessage());
        }

        return routes;
    }

    /**
     * Finds routes by departure city
     */
    public List<Record> findByDepartureCity(String city) {
        List<Record> routes = new ArrayList<>();
        String query = "SELECT * FROM routes WHERE departure_city = ? ORDER BY departure_time";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, city);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    routes.add(mapResultSetToRecord(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error finding routes by departure city: " + e.getMessage());
        }

        return routes;
    }

    /**
     * Finds routes by arrival city
     */
    public List<Record> findByArrivalCity(String city) {
        List<Record> routes = new ArrayList<>();
        String query = "SELECT * FROM routes WHERE arrival_city = ? ORDER BY departure_time";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, city);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    routes.add(mapResultSetToRecord(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error finding routes by arrival city: " + e.getMessage());
        }

        return routes;
    }

    /**
     * Finds a specific route by ID
     */
    public Record findByRouteId(String routeId) {
        String query = "SELECT * FROM routes WHERE route_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, routeId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRecord(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error finding route by ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Gets total count of routes
     */
    public int getRouteCount() {
        String query = "SELECT COUNT(*) as count FROM routes";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt("count");
            }

        } catch (SQLException e) {
            System.err.println("Error getting route count: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Maps a ResultSet row to a Record object
     */
    private Record mapResultSetToRecord(ResultSet rs) throws SQLException {
        String routeId = rs.getString("route_id");
        String departureCity = rs.getString("departure_city");
        String arrivalCity = rs.getString("arrival_city");
        LocalTime departureTime = LocalTime.parse(rs.getString("departure_time"), TIME_FMT);
        LocalTime arrivalTime = LocalTime.parse(rs.getString("arrival_time"), TIME_FMT);
        String trainType = rs.getString("train_type");
        String daysStr = rs.getString("days_of_operation");
        HashSet<DayOfWeek> daysOfOperation = dayParser.parseDays(daysStr);
        double firstClassRate = rs.getDouble("first_class_rate");
        double secondClassRate = rs.getDouble("second_class_rate");

        return new Record(routeId, departureCity, arrivalCity, departureTime, arrivalTime,
                trainType, daysOfOperation, firstClassRate, secondClassRate);
    }
}
