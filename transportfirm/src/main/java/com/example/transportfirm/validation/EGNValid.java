package com.example.transportfirm.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates that a String is a valid Bulgarian EGN (ЕГН):
 *  – exactly 10 digits
 *  – encodes a valid calendar date (YYMMDD, months 01-12 / 21-32 / 41-52)
 *  – passes the official MOD-11 checksum algorithm
 *
 * {@code null} values are considered valid (use @NotNull separately if required).
 */
@Documented
@Constraint(validatedBy = EGNValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface EGNValid {

    String message() default "Невалидно ЕГН";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
