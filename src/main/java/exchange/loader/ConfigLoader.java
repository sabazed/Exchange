package exchange.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import exchange.bus.MessageBus;
import exchange.services.MessageProcessor;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigLoader {

    private final ObjectMapper mapper;
    private ClassConfig classConfig;


    public ConfigLoader() {
        mapper = new ObjectMapper();
    }

    public void loadConfig() throws IOException {
        classConfig = mapper.readValue(getClass().getClassLoader().getResource("clsconfig.json"), ClassConfig.class);
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

    public String getGatewayId() {
        return classConfig.getGatewayId();
    }

    public List<MessageProcessor> getServices(MessageBus messageBus) {
        return classConfig.getServices().stream().map(serviceDecorator -> {
            try {
                Class<?> cls = Class.forName(serviceDecorator.getName());
                serviceDecorator.getParams().add(0, messageBus);
                serviceDecorator.getClasses().add(0, MessageBus.class);
                Constructor<?> constr = cls.getConstructor(serviceDecorator.getClasses().toArray(new Class[0]));
                return (MessageProcessor) constr.newInstance(serviceDecorator.getParams().toArray(new Object[0]));
            }
            catch (Exception e) {
                return null;
            }
        }).collect(Collectors.toList());
    }

}
