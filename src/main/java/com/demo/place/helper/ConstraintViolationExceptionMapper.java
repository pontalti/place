package com.demo.place.helper;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Provider
public class ConstraintViolationExceptionMapper
        implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {

        List<Map<String, String>> validationErrors = exception.getConstraintViolations()
                .stream()
                .map(this::toErrorMap)
                .toList();

        ErrorResponse body = new ErrorResponse(
                "Validation Failed",
                validationErrors
        );

        return Response
                .status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }

    private Map<String, String> toErrorMap(ConstraintViolation<?> violation) {
        Map<String, String> errorDetails = new LinkedHashMap<>();
        errorDetails.put("field", getPropertyPath(violation));
        errorDetails.put("message", violation.getMessage());
        return errorDetails;
    }

    private String getPropertyPath(ConstraintViolation<?> violation) {
        return violation.getPropertyPath() != null
                ? violation.getPropertyPath().toString()
                : "";
    }
}
