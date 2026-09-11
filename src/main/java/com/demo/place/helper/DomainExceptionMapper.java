package com.demo.place.helper;

import java.util.List;

import com.demo.place.exception.InvalidRequestException;
import com.demo.place.exception.ResourceNotFoundException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps the domain exceptions to their HTTP status.
 *
 * <p>This is the only place that knows a missing resource means 404 and a
 * broken business rule means 400, which keeps the service layer free of
 * transport concerns.
 */
@Provider
public class DomainExceptionMapper implements ExceptionMapper<RuntimeException> {

    @Override
    public Response toResponse(RuntimeException exception) {
        return switch (exception) {
            case ResourceNotFoundException e -> build(Response.Status.NOT_FOUND, "Not Found", e.getMessage());
            case InvalidRequestException e -> build(Response.Status.BAD_REQUEST, "Invalid Request", e.getMessage());
            default -> throw exception; // handled by GenericExceptionMapper
        };
    }

    private Response build(Response.Status status, String message, String detail) {
        return Response
                .status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(message, List.of(detail)))
                .build();
    }
}
