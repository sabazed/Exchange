package exchange.websocketendpoint;

public class ServiceInitializationException extends RuntimeException {

    private final String message;
    private final Throwable cause;

    public ServiceInitializationException(String message, Throwable throwable) {
        this.message = message;
        this.cause = throwable;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }

    @Override
    public String toString() {
        return "ServiceInitializationException: Exception occurred while creating service instances: " + message;
    }

}
