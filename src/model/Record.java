package model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;

public class Record {
    private String routeId;
    private String departureCity;
    private String arrivalCity;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private String trainType;
    private HashSet<DayOfWeek> daysOfOperation;
    private double firstClassRate;
    private double secondClassRate;

    public Record(String routeId, String departureCity, String arrivalCity,
                  LocalTime departureTime, LocalTime arrivalTime,
                  String trainType, HashSet<DayOfWeek> daysOfOperation,
                  double firstClassRate, double secondClassRate) {
        this.routeId = routeId;
        this.departureCity = departureCity;
        this.arrivalCity = arrivalCity;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.trainType = trainType;
        this.daysOfOperation = daysOfOperation;
        this.firstClassRate = firstClassRate;
        this.secondClassRate = secondClassRate;
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

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getTrainType() {
        return trainType;
    }

    public void setTrainType(String trainType) {
        this.trainType = trainType;
    }

    public HashSet<DayOfWeek> getDaysOfOperation() {
        return daysOfOperation;
    }

    public void setDaysOfOperation(HashSet<DayOfWeek> daysOfOperation) {
        this.daysOfOperation = daysOfOperation;
    }

    public double getFirstClassRate() {
        return firstClassRate;
    }

    public void setFirstClassRate(double firstClassRate) {
        this.firstClassRate = firstClassRate;
    }

    public double getSecondClassRate() {
        return secondClassRate;
    }

    public void setSecondClassRate(double secondClassRate) {
        this.secondClassRate = secondClassRate;
    }

    @Override
    public String toString() {
        return "Record{" +
                "routeId='" + routeId + '\'' +
                ", departureCity='" + departureCity + '\'' +
                ", arrivalCity='" + arrivalCity + '\'' +
                ", departureTime=" + departureTime +
                ", arrivalTime=" + arrivalTime +
                ", trainType='" + trainType + '\'' +
                ", daysOfOperation=" + daysOfOperation +
                ", firstClassRate=" + firstClassRate +
                ", secondClassRate=" + secondClassRate +
                '}';
    }
}