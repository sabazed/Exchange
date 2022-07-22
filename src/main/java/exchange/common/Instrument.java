package exchange.common;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

public class Instrument {

    private static int ID = 0;

    private final Integer id;
    private String name;

    public Instrument(String name) {
        this.name = name;
        this.id = ID++;
    }

    public Instrument() {
        id = ID++;
    }

    @JsonIgnore
    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Instrument && hashCode() == o.hashCode();
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

}
