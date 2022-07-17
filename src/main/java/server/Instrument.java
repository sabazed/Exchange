package server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Instrument {

    private Integer id;

    public Instrument(String id) {
        try {
            this.id = Integer.valueOf(id);
        }
        catch (NumberFormatException e) {
            this.id = null;
        }
    }

    @JsonCreator
    public Instrument(@JsonProperty("id") int id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Instrument && id != null && id.equals(((Instrument) o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
