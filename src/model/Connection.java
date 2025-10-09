package model;

import java.time.LocalTime;

public class Connection {
    private final Record record;

    public Connection(Record record) { this.record = record; }
    public String from()      { return record.getDepartureCity(); }
    public String to()        { return record.getArrivalCity(); }
    public LocalTime depTime(){ return record.getDepartureTime(); }
    public LocalTime arrTime(){ return record.getArrivalTime(); }
    public double firstRate() { return record.getFirstClassRate(); }
    public double secondRate(){ return record.getSecondClassRate(); }

    @Override
    public String toString() {
        return from() + " to " + to()
                + " | Departs: " + depTime()
                + " | Arrives: " + arrTime()
                + " | Train: " + record.getTrainType();
    }
}
