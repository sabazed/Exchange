package exchange.loader;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.stream.Collectors;

public class ServiceDecorator {

    private final String name;
    private final List<Object> params;
    private final List<Class<?>> classes;

    @JsonCreator
    public ServiceDecorator(@JsonProperty("name") String name,
                            @JsonProperty("params") List<Object> params) {
        this.name = name;
        this.params = params;
        this.classes = params.stream().map(param -> String.class).collect(Collectors.toList());
    }

    public String getName() {
        return name;
    }

    public List<Object> getParams() {
        return params;
    }

    @JsonIgnore
    public List<Class<?>> getClasses() {
        return classes;
    }

}
