package entities;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;

public class Route {
    private String routeId;
    private String departureCity;
    private String arrivalCity;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private String trainType;
    private HashSet<DayOfWeek> daysOfOperation;
    private double firstClassTicketRate;
    private double secondClassTicketRate;

    public Route(String routeId, String departureCity, String arrivalCity, LocalTime departureTime, LocalTime arrivalTime,
                 String trainType, HashSet<DayOfWeek> daysOfOperation, double firstClassTicketRate, double secondClassTicketRate) {
        this.routeId = routeId;
        this.departureCity = departureCity;
        this.arrivalCity = arrivalCity;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.trainType = trainType;
        this.daysOfOperation = daysOfOperation;
        this.firstClassTicketRate = firstClassTicketRate;
        this.secondClassTicketRate = secondClassTicketRate;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getDepartureCity() {
        return departureCity;
    }

    public void setDepartureCity(String departureCity) {
        this.departureCity = departureCity;
    }

    public String getArrivalCity() {
        return arrivalCity;
    }

    public void setArrivalCity(String arrivalCity) {
        this.arrivalCity = arrivalCity;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    @Override
    public String toString() {
        return "Route{" +
                "routeId='" + routeId + '\'' +
                ", departureCity='" + departureCity + '\'' +
                ", arrivalCity='" + arrivalCity + '\'' +
                ", departureTime='" + departureTime + '\'' +
                ", arrivalTime='" + arrivalTime + '\'' +
                ", trainType='" + trainType + '\'' +
                ", daysOfOperation='" + daysOfOperation + '\'' +
                ", firstClassTicketRate=" + firstClassTicketRate +
                ", secondClassTicketRate=" + secondClassTicketRate +
                '}';
    }
}
