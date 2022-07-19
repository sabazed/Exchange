package server;

public class EncoderException extends RuntimeException {

    private final Message response;
    private final Throwable throwable;

    public EncoderException(Message response, Throwable throwable) {
        this.response = response;
        this.throwable = throwable;
    }

    public Message getResponse() {
        return response;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public String toString() {
        return "EncoderException: Exception occurred while encoding: " + response;
    }
}
