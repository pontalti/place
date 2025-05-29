package com.demo.place.helper;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class ExceptionHelper extends ResponseEntityExceptionHandler {

    public ExceptionHelper() {
        super();
    }

    @Override
    @Nullable
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex,
                                                             @Nullable Object body,
                                                             HttpHeaders headers,
                                                             HttpStatusCode statusCode,
                                                             WebRequest request) {

        List<String> details = List.of(ex.getLocalizedMessage());
        ErrorResponse error = new ErrorResponse("Internal Server Error", details);
        return new ResponseEntity<>(error, statusCode);
    }

    @Override
    @Nullable
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
                                                                            HttpHeaders headers,
                                                                            HttpStatusCode status,
                                                                            WebRequest request) {
        List<Map<String, String>> validationErrors = new ArrayList<>();

        ex.getAllErrors().forEach(error -> {
            Map<String, String> errorDetails = new LinkedHashMap<>();
            String fieldPath = error instanceof FieldError
                    ? ((FieldError) error).getField()
                    : error.toString();

            errorDetails.put("field", fieldPath);
            errorDetails.put("message", error.getDefaultMessage());
            validationErrors.add(errorDetails);
        });

        ErrorResponse error = new ErrorResponse("Validation Failed", validationErrors);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @Override
    @Nullable
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {

        List<String> details = List.of(ex.getMostSpecificCause().getMessage());
        ErrorResponse error = new ErrorResponse("Malformed JSON or unreadable request", details);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
