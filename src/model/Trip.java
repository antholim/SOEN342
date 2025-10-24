package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Trip {
    private final String tripId;
    private final List<Reservation> reservations = new ArrayList<>();

    public Trip(String tripId) { this.tripId = tripId; }

    public String tripId() { return tripId; }
    public List<Reservation> reservations() { return Collections.unmodifiableList(reservations); }

    public void addReservation(Reservation r) {
        if (reservations.contains(r)) {
            throw new IllegalArgumentException("Client already has a reservation on this connection");
        }
        reservations.add(r);
    }
}
