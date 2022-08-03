package exchange.loader;

public class InvalidClassConfigException extends RuntimeException {

    private final String message;
    private Throwable cause;

    public InvalidClassConfigException(String message) {
        this.message = message;
    }

    public InvalidClassConfigException(Throwable throwable) {
        cause = throwable;
        message = throwable.getMessage();
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
        return "InvalidClassConfigException: Exception occurred while reading the config file: " + message;
    }

}
