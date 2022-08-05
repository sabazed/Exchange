package exchange.loader;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ServiceDecorator {

    private final String name;
    private final Map<String, ServiceDecoratorParameter> params;

    @JsonCreator
    public ServiceDecorator(@JsonProperty("name") String name,
                            @JsonProperty("params") List<ServiceDecoratorParameter> params) {
        this.name = name;
        this.params = params.stream().collect(Collectors.toMap(ServiceDecoratorParameter::getParamName, param -> param));
    }

    public String getName() {
        return name;
    }

    public Set<String> getParamNames() {
        return params.keySet();
    }

    public Object getParamValue(String key) {
        return params.get(key).getParamValue();
    }

    public Class<?> getParamClass(String key) throws ClassNotFoundException {
        return params.get(key).getParamClass();
    }

}
