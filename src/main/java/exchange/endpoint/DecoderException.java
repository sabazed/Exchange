package exchange.endpoint;

public class DecoderException extends RuntimeException {

    private final String message;
    private final Throwable cause;

    public DecoderException(String message, Throwable throwable) {
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
        return "DecoderException: Exception occurred while decoding json text: " + message;
    }
}
