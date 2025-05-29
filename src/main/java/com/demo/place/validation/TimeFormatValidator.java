package com.demo.place.validation;

import com.demo.place.annotation.ValidTime;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
public class TimeFormatValidator implements ConstraintValidator<ValidTime, String> {

    private String pattern;

    @Override
    public void initialize(ValidTime constraintAnnotation) {
        this.pattern = constraintAnnotation.pattern();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            LocalTime.parse(value, formatter);
            return true;
        } catch (DateTimeParseException e) {
            log.error("Error on TimeFormatValidator -> {0}", e);
        }
        return false;
    }
}
