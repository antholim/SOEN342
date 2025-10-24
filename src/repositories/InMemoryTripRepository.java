package repositories;

import model.Trip;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InMemoryTripRepository {
    private final List<Trip> trips = new ArrayList<>();

    public void save(Trip t) {
        trips.add(t);
    }

    public List<Trip> all() {
        return Collections.unmodifiableList(trips);
    }
}
