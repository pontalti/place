package com.demo.place.helper;

import java.util.List;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Catch-all for the remaining JAX-RS status exceptions (404 on an unknown
 * path, 405, 406, 415 and so on), so they come back in the same
 * {@link ErrorResponse} shape the rest of the API uses instead of the
 * runtime's default HTML/JSON page.
 *
 * <p>The original status is always preserved — rewriting it is what made the
 * previous mapper turn every error into a 400.
 */
@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Override
    public Response toResponse(WebApplicationException exception) {
        Response original = exception.getResponse();
        int status = original.getStatus();

        // Already carries a body built elsewhere (e.g. by another mapper).
        if (original.hasEntity()) {
            return original;
        }

        Response.Status resolved = Response.Status.fromStatusCode(status);
        String message = resolved != null ? resolved.getReasonPhrase() : "Error";
        String detail = exception.getMessage();

        ErrorResponse body = new ErrorResponse(
                message,
                detail != null ? List.of(detail) : List.of()
        );

        return Response
                .status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
