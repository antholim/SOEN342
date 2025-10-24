package service;

import model.Connection;
import model.Reservation;
import model.Ticket;
import model.Trip;
import model.Traveller;
import repositories.ClientRepository;
import repositories.TripCSVRepository; // Change this import

import java.util.List;
import java.util.StringJoiner;

public class BookingService {
    private final TripCSVRepository tripRepo; // Change type
    private final ClientRepository clientRepo;

    public BookingService(TripCSVRepository tripRepo, ClientRepository clientRepo) { // Change parameter type
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
        String tripId = IdGenerator.newTripId(10);
        Trip t = new Trip(tripId);
        for (Traveller traveller : travellers) {
            clientRepo.save(traveller);
            var ticket = new Ticket(IdGenerator.nextTicket());
            var reservation = new Reservation(traveller, connectionAnchor, ticket);
            t.addReservation(reservation);
        }
        tripRepo.saveTrip(t); // Changed from save() to saveTrip()
        return t;
    }
}