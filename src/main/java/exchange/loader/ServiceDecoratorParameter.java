package exchange.loader;

public class ServiceDecoratorParameter {

    private String paramName;
    private Object paramValue;
    private Class<?> paramClass;

    public String getParamName() {
        return paramName;
    }

    public Object getParamValue() {
        return paramValue;
    }

    public Class<?> getParamClass() {
        return paramClass;
    }
}
