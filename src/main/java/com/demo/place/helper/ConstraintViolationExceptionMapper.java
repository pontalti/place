package com.demo.place.helper;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Turns Bean Validation failures into the same 400 payload the Spring branch
 * produces, so a single Angular client can talk to either backend.
 *
 * <p>Without this mapper the runtime answers 400 with an empty body, and the
 * client has nothing to show beyond the status code.
 */
@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {

        List<Map<String, String>> validationErrors = exception.getConstraintViolations()
                .stream()
                // Violations arrive in a Set, so the order changes between runs.
                // Sorting keeps the response stable, which matters for tests and
                // for anyone diffing two responses.
                .sorted(Comparator.comparing(this::fieldPath)
                        .thenComparing(ConstraintViolation::getMessage))
                .map(this::toErrorMap)
                .toList();

        ErrorResponse body = new ErrorResponse("Validation Failed", validationErrors);

        return Response
                .status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }

    private Map<String, String> toErrorMap(ConstraintViolation<?> violation) {
        Map<String, String> errorDetails = new LinkedHashMap<>();
        errorDetails.put("field", fieldPath(violation));
        errorDetails.put("invalidValue", invalidValue(violation));
        errorDetails.put("message", violation.getMessage());
        return errorDetails;
    }

    /**
     * Strips the method and parameter nodes from the property path.
     *
     * <p>Method-level validation reports a path such as
     * {@code savePlace.place.days[0].startTime}, where the first two nodes are
     * the resource method and its argument. Spring reports only
     * {@code days[0].startTime}, so dropping them is what makes the two
     * backends agree on the field name.
     */
    private String fieldPath(ConstraintViolation<?> violation) {
        Path path = violation.getPropertyPath();
        if (path == null) {
            return "";
        }

        String field = StreamSupport.stream(path.spliterator(), false)
                .filter(node -> node.getKind() != ElementKind.METHOD
                        && node.getKind() != ElementKind.PARAMETER)
                .map(this::render)
                .filter(segment -> !segment.isEmpty())
                .collect(Collectors.joining("."));

        // A class-level constraint (@StartBeforeEnd, @NonEmptyPatch) has nothing
        // left after the filter, since it is not attached to any property.
        return field.isEmpty() ? path.toString() : field;
    }

    /** Re-attaches the collection index that the node carries separately. */
    private String render(Path.Node node) {
        String name = node.getName() == null ? "" : node.getName();
        return node.getIndex() == null ? name : name + "[" + node.getIndex() + "]";
    }

    private String invalidValue(ConstraintViolation<?> violation) {
        Object value = violation.getInvalidValue();
        return value == null ? "null" : value.toString();
    }
}