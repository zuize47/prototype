package hoangnd.web.app.domain.exception;

public class InternalServerError extends RuntimeException {

    public InternalServerError () {
    }

    public InternalServerError (final String message) {
        super(message);
    }

    public InternalServerError (final String message, final Throwable cause) {
        super(message, cause);
    }

    public InternalServerError (final Throwable cause) {
        super(cause);
    }

}
