package exchange.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import exchange.bus.MessageBus;
import exchange.services.MessageProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigLoader {

    private static final Logger LOG = LogManager.getLogger(ConfigLoader.class);

    private final ObjectMapper mapper;
    private ClassConfig classConfig;


    public ConfigLoader() {
        mapper = new ObjectMapper();
    }

    public void loadConfig() {
        try {
            classConfig = mapper.readValue(getClass().getClassLoader().getResource("clsconfig.json"), ClassConfig.class);
        } catch (IOException e) {
            LOG.error("Couldn't read config file", e);
            throw new InvalidClassConfigException(e);
        }
    }

    public MessageBus getBusInstance() {
        try {
            Class<?> cls = Class.forName(classConfig.getBusName());
            Constructor<?> constr = cls.getConstructor();
            return (MessageBus) constr.newInstance();
        }
        catch (ReflectiveOperationException | NullPointerException e) {
            LOG.error("Couldn't create class instance", e);
            throw new InvalidClassConfigException(e);
        }
    }

    public String getEndpointId() {
        return classConfig.getEndpointId();
    }

    public String getGatewayId() {
        return classConfig.getGatewayId();
    }

    public List<MessageProcessor> getServices(MessageBus messageBus) {
        return classConfig.getServices().stream().map(decorator -> {
            try {
                Class<?> cls = Class.forName(decorator.getName());
                Constructor<?> constr = cls.getConstructor(MessageBus.class);
                MessageProcessor processor = (MessageProcessor) constr.newInstance(messageBus);
                for (String key : decorator.getParamNames()) {
                    Method setter = processor.getClass().getMethod("set" + key, decorator.getParamClass(key));
                    setter.invoke(processor, decorator.getParamValue(key));
                }
                return processor;
            }
            catch (ReflectiveOperationException e) {
                LOG.error("Couldn't create class instance", e);
                throw new InvalidClassConfigException(e);
            }
        }).collect(Collectors.toList());
    }

}
