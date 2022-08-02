package exchange.loader;

import java.util.List;

public class ClassConfig {

    private String endpointId;
    private String gatewayId;
    private String busName;
    private List<ServiceDecorator> services;

    public String getEndpointId() {
        return endpointId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public String getBusName() {
        return busName;
    }

    public List<ServiceDecorator> getServices() {
        return services;
    }

}
