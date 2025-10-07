public class Records {
    private String routeId;
    private String departureCity;
    private String arrivalCity;
    private String departureTime;
    private String arrivalTime;
    private String trainType;
    private String daysOfOperation;
    private double firstClassRate;
    private double secondClassRate;

    public Records(String routeId, String departureCity, String arrivalCity,
                      String departureTime, String arrivalTime,
                      String trainType, String daysOfOperation,
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
}