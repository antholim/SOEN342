package model;

import java.util.Objects;

public class Traveller {
    private final String firstName;
    private final String lastName;
    private final int age;
    private final String id; 

    public Traveller(String firstName, String lastName, int age, String id) {
        this.firstName = firstName;
        this.lastName  = lastName;
        this.age       = age;
        this.id        = id;
    }

    public String firstName() { return firstName; }
    public String lastName()  { return lastName; }
    public int age()          { return age; }
    public String id()        { return id; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Traveller t)) return false;
        return lastName.equalsIgnoreCase(t.lastName) && id.equals(t.id);
    }
    @Override public int hashCode() { return Objects.hash(lastName.toLowerCase(), id); }
}
