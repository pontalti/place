package com.demo.place.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.demo.place.validation.NonEmptyPatchValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NonEmptyPatchValidator.class)
@Documented
public @interface NonEmptyPatch {
    String message() default "The PATCH request must contain at least one field for updating, excluding the ID.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

