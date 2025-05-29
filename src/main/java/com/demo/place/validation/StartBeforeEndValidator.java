package com.demo.place.validation;

import com.demo.place.annotation.StartBeforeEnd;
import com.demo.place.records.DayOpeningRecord;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@Slf4j
public class StartBeforeEndValidator
        implements ConstraintValidator<StartBeforeEnd, DayOpeningRecord> {

    @Override
    public boolean isValid(DayOpeningRecord rec, ConstraintValidatorContext ctx) {
        if (rec == null) {
            return true;
        }

        try {
            LocalTime start = LocalTime.parse(rec.startTime());
            LocalTime end = LocalTime.parse(rec.endTime());

            if (start.equals(end)) {
                return false;
            }

            if (end.equals(LocalTime.MIDNIGHT)) {
                return true;
            }

            return start.isBefore(end);
        } catch (DateTimeParseException ex) {
            log.error("The ValidTime annotation will validate");
            return true;
        }
    }
}