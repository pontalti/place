package com.demo.place.helper;

import java.util.List;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class BadRequestExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Override
    public Response toResponse(WebApplicationException exception) {
        Throwable cause = exception.getCause();
        String detail = cause != null ? cause.getMessage() : exception.getMessage();

        ErrorResponse body = new ErrorResponse(
                "Malformed JSON or unreadable request",
                List.of(detail)
        );

        return Response
                .status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
