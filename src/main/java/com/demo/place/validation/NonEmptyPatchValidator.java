package com.demo.place.validation;

import java.lang.reflect.Field;

import com.demo.place.annotation.NonEmptyPatch;
import com.demo.place.records.PlacePatchRecord;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NonEmptyPatchValidator implements ConstraintValidator<NonEmptyPatch, PlacePatchRecord> {

    @Override
    public boolean isValid(PlacePatchRecord record, ConstraintValidatorContext context) {
        if (record == null) {
            return true; 
        }

        try {
            for (Field field : record.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                
                if (field.getName().equals("id")) {
                    continue; 
                }
                
                if (field.get(record) != null) {
                    return true;
                }
            }
        } catch (IllegalAccessException e) {
             throw new RuntimeException("Error inspecting Record fields.", e);
        }

        return false; 
    }
}