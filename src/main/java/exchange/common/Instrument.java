package exchange.common;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.Objects;

@Entity
public class Instrument {

    @Id
    private Integer id;

    private String name;

    public Instrument(String name, Integer id) {
        this.name = name;
        this.id = id;
    }

    public Instrument() {

    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Instrument other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "{id=" + id + ", name='" + name + "'}";
    }
}
