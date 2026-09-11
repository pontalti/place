package com.demo.place.helper;

import java.util.List;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Handles an unreadable request body (malformed JSON, wrong shape).
 *
 * <p>Scoped to {@link BadRequestException} on purpose: the previous version
 * mapped {@code WebApplicationException}, which is the supertype of every
 * JAX-RS status exception, so a 404 raised by the service was rewritten as a
 * 400 "malformed JSON". JAX-RS picks the mapper whose generic type is the
 * nearest supertype, so a broad mapper silently wins over a specific one.
 */
@Provider
public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException> {

    @Override
    public Response toResponse(BadRequestException exception) {
        Throwable cause = exception.getCause();
        String detail = cause != null ? cause.getMessage() : exception.getMessage();

        ErrorResponse body = new ErrorResponse(
                "Malformed JSON or unreadable request",
                List.of(detail != null ? detail : "Unreadable request body")
        );

        return Response
                .status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
