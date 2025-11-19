package com.demo.place.helper;

import java.util.List;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {


    @Override
    public Response toResponse(Throwable exception) {

        if (exception instanceof WebApplicationException wae) {
            // já tem Response correto (400, 404, 405, etc.)
            return wae.getResponse();
        }

        log.error("Unexpected error", exception);

        var error = new ErrorResponse(
                "Internal Server Error",
                List.of(exception.getMessage())
        );

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                       .entity(error)
                       .build();
    }
}

