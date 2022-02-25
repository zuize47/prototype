package hoangnd.web.app.config;

import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import hoangnd.web.app.domain.exception.NotFoundException;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@ControllerAdvice
@Log4j2
public class GlobalExceptionHandler {

    public ResponseEntity<ApiCallError<String>> handleNotFoundException (final HttpServletRequest request,
                                                                         final NotFoundException ex) {
        log.error("NotFoundException {}\n", request.getRequestURI(), ex);

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ApiCallError<>("Not found exception", List.of(ex.getMessage())));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiCallError<String>> handleValidationException (final HttpServletRequest request,
                                                                           final ValidationException ex) {
        log.error("ValidationException {}\n", request.getRequestURI(), ex);

        return ResponseEntity
            .badRequest()
            .body(new ApiCallError<>("Validation exception", List.of(ex.getMessage())));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiCallError<String>> handleMissingServletRequestParameterException (
        final HttpServletRequest request, final MissingServletRequestParameterException ex) {
        log.error("handleMissingServletRequestParameterException {}\n", request.getRequestURI(), ex);

        return ResponseEntity
            .badRequest()
            .body(new ApiCallError<>("Missing request parameter", List.of(ex.getMessage())));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiCallError<Map<String, String>>> handleMethodArgumentTypeMismatchException (
        final HttpServletRequest request, final MethodArgumentTypeMismatchException ex) {
        log.error("handleMethodArgumentTypeMismatchException {}\n", request.getRequestURI(), ex);

        Map<String, String> details = new HashMap<>();
        details.put("paramName", ex.getName());
        details.put("paramValue", ofNullable(ex.getValue()).map(Object::toString).orElse(""));
        details.put("errorMessage", ex.getMessage());

        return ResponseEntity
            .badRequest()
            .body(new ApiCallError<>("Method argument type mismatch", List.of(details)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiCallError<Map<String, String>>> handleMethodArgumentNotValidException (
        final HttpServletRequest request, final MethodArgumentNotValidException ex) {
        log.error("handleMethodArgumentNotValidException {}\n", request.getRequestURI(), ex);

        List<Map<String, String>> details = new ArrayList<>();
        ex.getBindingResult()
            .getFieldErrors()
            .forEach(fieldError -> {
                Map<String, String> detail = new HashMap<>();
                detail.put("objectName", fieldError.getObjectName());
                detail.put("field", fieldError.getField());
                detail.put("rejectedValue", "" + fieldError.getRejectedValue());
                detail.put("errorMessage", fieldError.getDefaultMessage());
                details.add(detail);
            });

        return ResponseEntity
            .badRequest()
            .body(new ApiCallError<>("Method argument validation failed", details));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiCallError<String>> handleAccessDeniedException (
        final HttpServletRequest request, final AccessDeniedException ex) {
        log.error("handleAccessDeniedException {}\n", request.getRequestURI(), ex);

        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(new ApiCallError<>("Access denied!", List.of(ex.getMessage())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiCallError<String>> handleInternalServerError (
        final HttpServletRequest request, final Exception ex) {
        log.error("handleInternalServerError {}\n", request.getRequestURI(), ex);

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiCallError<>("Internal server error", List.of(ex.getMessage())));
    }

    @Data
    @NoArgsConstructor
    public static class ApiCallError<T> {

        private String  message;
        private List<T> details;

        public ApiCallError (final String message, final List<T> details) {
            this.message = message;
            this.details = details == null ? null : List.copyOf(details);
        }

        public void setDetails (final List<T> details) {
            this.details = details == null ? null : List.copyOf(details);
        }

        public List<T> getDetails () {
            return details == null ? null : List.copyOf(details);
        }
    }
}

