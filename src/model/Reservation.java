package model;

import java.util.Objects;

public class Reservation {
    private final Traveller traveller;
    private final Connection connection;
    private final Ticket ticket;

    public Reservation(Traveller traveller, Connection connection, Ticket ticket) {
        this.traveller = traveller;
        this.connection = connection;
        this.ticket = ticket;
    }

    public Traveller traveller() { return traveller; }
    public Connection connection() { return connection; }
    public Ticket ticket() { return ticket; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation r)) return false;
        return Objects.equals(traveller, r.traveller) && Objects.equals(connection, r.connection);
    }
    @Override public int hashCode() { return Objects.hash(traveller, connection); }
}
