//package service;
//
//import java.util.ArrayList;
//import java.util.List;
//import model.Record;
//
//public class Search {
//
//    private List<Record> records;
//
//    public Search(List<Record> records) {
//        this.records = records;
//    }
//
//    public List<Record> searchAdvanced(
//        String departure,
//        String arrival,
//        String trainType,
//        String day,
//        Double maxSecondClassPrice)
//
//    {
//    List<Record> results = new ArrayList<>();
//
//    for (Record r : records) {
//        boolean match = true;
//
//        if (departure != null && !departure.isEmpty() &&
//                !r.getDepartureCity().equalsIgnoreCase(departure)) {
//            match = false;
//        }
//
//        if (arrival != null && !arrival.isEmpty() &&
//                !r.getArrivalCity().equalsIgnoreCase(arrival)) {
//            match = false;
//        }
//
//        if (trainType != null && !trainType.isEmpty() &&
//                !r.getTrainType().equalsIgnoreCase(trainType)) {
//            match = false;
//        }
//
//        if (day != null && !day.isEmpty() &&
//                !r.getDaysOfOperation().toLowerCase().contains(day.toLowerCase())) {
//            match = false;
//        }
//
//        if (maxSecondClassPrice != null &&
//                r.getSecondClassRate() > maxSecondClassPrice) {
//            match = false;
//        }
//
//        if (match) results.add(r);
//    }
//
//    return results;
//}
//
//
//}
