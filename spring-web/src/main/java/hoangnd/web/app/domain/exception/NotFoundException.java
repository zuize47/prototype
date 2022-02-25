package hoangnd.web.app.domain.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException (final String message) {
        super(message);
    }

    public NotFoundException (final Class<?> clazz, final long id) {
        super(String.format("Entity %s with id %d not found", clazz.getSimpleName(), id));
    }

    public NotFoundException (final Class<?> clazz, final String id) {
        super(String.format("Entity %s with id %s not found", clazz.getSimpleName(), id));
    }
}
