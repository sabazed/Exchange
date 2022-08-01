package exchange.services;

public class IllegalMessageException extends RuntimeException {

    private final String message;

    public IllegalMessageException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "IllegalMessageException: Exception occurred while processing the message: " + message;
    }

}
