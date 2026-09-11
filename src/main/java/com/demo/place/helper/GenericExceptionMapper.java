package com.demo.place.helper;

import java.util.List;
import java.util.UUID;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

/**
 * Last resort for anything no other mapper handles.
 *
 * <p>The exception message is logged, never returned: an unhandled failure can
 * carry SQL fragments, class names or file paths. The client gets a
 * correlation id instead, which is enough to find the entry in the log.
 */
@Slf4j
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        // More specific mappers normally win, but a WebApplicationException can
        // still reach here when thrown from a provider or a filter.
        if (exception instanceof WebApplicationException webAppEx) {
            return webAppEx.getResponse();
        }

        String errorId = UUID.randomUUID().toString();
        log.error("Unexpected error [{}]", errorId, exception);

        ErrorResponse body = new ErrorResponse(
                "Internal Server Error",
                List.of("Unexpected error. Reference: " + errorId)
        );

        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
