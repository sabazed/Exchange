package exchange.loader;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ServiceDecorator {

    private final String name;
    private final Map<String, Object> params;
    private final Map<String, Class<?>> paramClasses;

    @JsonCreator
    public ServiceDecorator(@JsonProperty("name") String name,
                            @JsonProperty("params") List<ServiceDecoratorParameter> params) throws ClassNotFoundException {
        this.name = name;
        this.params = new HashMap<>();
        this.paramClasses = new HashMap<>();
        for (ServiceDecoratorParameter param : params) {
            this.params.put(param.getParamName(), param.getParamValue());
            this.paramClasses.put(param.getParamName(), Class.forName(param.getParamClass()));
        }
    }

    public String getName() {
        return name;
    }

    public Object getParam(String key) {
        return params.get(key);
    }

    public Class<?> getParamClass(String key) {
        return paramClasses.get(key);
    }

    public Set<String> getKeys() {
        return params.keySet();
    }

}
