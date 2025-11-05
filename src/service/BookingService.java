package service;

import model.Connection;
import model.Reservation;
import model.Ticket;
import model.Trip;
import model.Traveller;
import repositories.ClientRepository;
import repositories.TripDatabaseRepository;

import java.util.List;
import java.util.StringJoiner;

public class BookingService {
    private final TripDatabaseRepository tripRepo;
    private final ClientRepository clientRepo;

    public BookingService(TripDatabaseRepository tripRepo, ClientRepository clientRepo) {
        this.tripRepo = tripRepo;
        this.clientRepo = clientRepo;
    }

    public Trip bookDirect(List<Traveller> travellers, model.Record r) {
        String itinerary = r.getDepartureCity() + " → " + r.getArrivalCity() + " " + r.getDepartureTime() + "-" + r.getArrivalTime() + " (" + r.getTrainType() + ")";
        return persist(travellers, itinerary, new Connection(r));
    }

    public Trip bookConnection(List<Traveller> travellers, List<Connection> legs) {
        StringJoiner sj = new StringJoiner(" | ");
        for (Connection c : legs) sj.add(c.from() + "→" + c.to() + " " + c.depTime() + "-" + c.arrTime());
        String itinerary = sj.toString();
        Connection anchor = legs.get(0);
        return persist(travellers, itinerary, anchor);
    }

    private Trip persist(List<Traveller> travellers, String itinerary, Connection connectionAnchor) {
        // Create a temporary trip with placeholder ID
        Trip t = new Trip("temp");
        for (Traveller traveller : travellers) {
            clientRepo.save(traveller);
            var ticket = new Ticket(IdGenerator.nextTicket());
            var reservation = new Reservation(traveller, connectionAnchor, ticket);
            t.addReservation(reservation);
        }
        
        // Save to database and get the actual numeric trip ID
        long numericTripId = tripRepo.saveTrip(t);
        
        // Return a new Trip object with the correct numeric ID
        return new Trip(String.valueOf(numericTripId));
    }
}