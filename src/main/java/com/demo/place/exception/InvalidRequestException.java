package com.demo.place.exception;

/**
 * Thrown when the request is syntactically valid but breaks a business rule
 * (for example, an update without an id).
 *
 * <p>Kept free of JAX-RS types so the service layer stays portable across
 * runtimes — the mapping to an HTTP status lives in the mapper.
 */
public class InvalidRequestException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public InvalidRequestException(String message) {
        super(message);
    }
}
