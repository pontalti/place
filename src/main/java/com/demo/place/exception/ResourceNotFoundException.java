package com.demo.place.exception;

/**
 * Thrown when a requested resource does not exist.
 *
 * <p>Kept free of JAX-RS types so the service layer stays portable across
 * runtimes — the mapping to an HTTP status lives in the mapper.
 */
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException place(Long id) {
        return new ResourceNotFoundException("Place not found: " + id);
    }
}
