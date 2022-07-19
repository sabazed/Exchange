package server;

public class DecoderException extends RuntimeException {

    private final String message;
    private final Throwable throwable;

    public DecoderException(String message, Throwable throwable) {
        this.message = message;
        this.throwable = throwable;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public String toString() {
        return "DecoderException: Exception occurred while decoding json text: " + message;
    }
}
