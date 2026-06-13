package com.henrique.stickermarker.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Centralised exception handler that maps exceptions thrown anywhere in the controller
 * layer to consistent HTTP error responses.
 *
 * <p>All error bodies follow the same shape: {@code {"error": "<message>"}}, so the
 * frontend can handle errors uniformly regardless of type.</p>
 *
 * <p>Note: the {@link RuntimeException} handler maps to {@code 404 NOT_FOUND} as a
 * convention — services throw {@code RuntimeException} when a requested resource does not
 * exist. Consider introducing a dedicated {@code ResourceNotFoundException} for clarity
 * if the codebase grows.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles Bean Validation failures ({@code @Valid} on request bodies).
     * Aggregates all field errors into a single comma-separated string so the client
     * receives all validation issues in one response rather than the first failure only.
     *
     * @param ex the validation exception containing one or more field errors
     * @return 400 Bad Request with a combined error message
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", errors));
    }

    /**
     * Handles failed authentication attempts (wrong email/password).
     * Returns a generic message instead of the internal exception message to avoid
     * revealing whether the email exists in the system.
     *
     * @param ex the bad credentials exception
     * @return 401 Unauthorized with a generic credential error message
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid email or password"));
    }

    /**
     * Handles business rule violations thrown as {@link IllegalArgumentException}.
     * Services use this exception type for invalid inputs that pass bean validation
     * but violate domain rules (e.g. duplicate friend request, private collection access).
     *
     * @param ex the exception with a descriptive message for the client
     * @return 400 Bad Request with the exception message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    /**
     * Catch-all for unchecked exceptions not handled by more specific handlers.
     * Primarily catches {@code RuntimeException} thrown by services when a resource
     * is not found — mapped to 404 by convention.
     *
     * @param ex the runtime exception
     * @return 404 Not Found with the exception message
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }
}
