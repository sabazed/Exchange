package exchange.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import exchange.bus.MessageBus;
import exchange.services.MessageProcessor;

import java.io.IOException;
import java.lang.reflect.Constructor;

public class ConfigLoader {

    private final ObjectMapper mapper;
    private ClassConfig classConfig;

    public ConfigLoader() {
        mapper = new ObjectMapper();
    }

    public void loadConfig() throws IOException {
        classConfig = mapper.readValue(getClass().getClassLoader().getResource("clsconfig.json"), ClassConfig.class);
    }

    public MessageProcessor getEngineInstance(MessageBus bus) throws ReflectiveOperationException {
        if (classConfig == null) throw new NullPointerException("Class configuration has not been loaded");
        Class<?> cls = Class.forName(classConfig.getEngineName());
        Constructor<?> constr = cls.getConstructor(MessageBus.class, String.class, String.class);
        return (MessageProcessor) constr.newInstance(bus, classConfig.getGatewayId(), classConfig.getEngineId());
    }

    public MessageProcessor getGatewayInstance(MessageBus bus) throws ReflectiveOperationException {
        if (classConfig == null) throw new NullPointerException("Class configuration has not been loaded");
        Class<?> cls = Class.forName(classConfig.getGatewayName());
        Constructor<?> constr = cls.getConstructor(MessageBus.class, String.class, String.class, String.class, String.class);
        return (MessageProcessor) constr.newInstance(bus, classConfig.getEngineId(), classConfig.getProviderId(), classConfig.getEndpointId(), classConfig.getGatewayId());
    }

    public MessageProcessor getProviderInstance(MessageBus bus) throws ReflectiveOperationException {
        if (classConfig == null) throw new NullPointerException("Class configuration has not been loaded");
        Class<?> cls = Class.forName(classConfig.getProviderName());
        Constructor<?> constr = cls.getConstructor(MessageBus.class, String.class, String.class);
        return (MessageProcessor) constr.newInstance(bus, classConfig.getGatewayId(), classConfig.getProviderId());
    }

    public MessageBus getBusInstance() throws ReflectiveOperationException {
        if (classConfig == null) throw new NullPointerException("Class configuration has not been loaded");
        Class<?> cls = Class.forName(classConfig.getBusName());
        Constructor<?> constr = cls.getConstructor();
        return (MessageBus) constr.newInstance();
    }

    public String getEndpointId() {
        return classConfig.getEndpointId();
    }

}
