package repositories;

import model.Traveller;
import java.util.HashSet;
import java.util.Set;

public class ClientRepository {
    private final Set<Traveller> clients = new HashSet<>();

    public void save(Traveller t) {
        clients.add(t);
    }

    public boolean exists(Traveller t) {
        return clients.contains(t);
    }

    public int count() {
        return clients.size();
    }

    public Set<Traveller> findAll() {
        return Set.copyOf(clients);
    }
}
