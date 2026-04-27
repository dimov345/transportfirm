package com.example.transportfirm.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Implementation of the Bulgarian EGN checksum validator.
 *
 * Algorithm specification:
 *   digits[0..9] where digit[9] is the control digit.
 *
 *   Weights: 2, 4, 8, 5, 10, 9, 7, 3, 6
 *   sum = Σ (digits[i] * weights[i])  for i = 0..8
 *   remainder = sum % 11
 *   control  = remainder < 10 ? remainder : 0
 *   valid    = control == digits[9]
 *
 * Date encoding:
 *   YY MM DD — where MM:
 *     01-12  → born 1900–1999
 *     21-32  → born 2000–2099 (month - 20)
 *     41-52  → born 1800–1899 (month - 40)
 */
public class EGNValidator implements ConstraintValidator<EGNValid, String> {

    private static final int[] WEIGHTS = { 2, 4, 8, 5, 10, 9, 7, 3, 6 };

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null is handled by @NotNull — treat as valid here
        if (value == null) {
            return true;
        }

        // Must be exactly 10 digits
        if (!value.matches("\\d{10}")) {
            return false;
        }

        int[] d = new int[10];
        for (int i = 0; i < 10; i++) {
            d[i] = value.charAt(i) - '0';
        }

        // Validate encoded date
        if (!isValidDate(d)) {
            return false;
        }

        // Checksum (MOD-11)
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += d[i] * WEIGHTS[i];
        }
        int remainder = sum % 11;
        int expectedControl = remainder < 10 ? remainder : 0;

        return expectedControl == d[9];
    }

    /**
     * Validates the date encoded in the first 6 digits of the EGN.
     * Digits 0-1 = year (YY), digits 2-3 = month (MM, adjusted), digits 4-5 = day (DD).
     */
    private boolean isValidDate(int[] d) {
        int year  = d[0] * 10 + d[1];
        int month = d[2] * 10 + d[3];
        int day   = d[4] * 10 + d[5];

        // Adjust month offset for century
        int realMonth;
        if (month >= 1 && month <= 12) {
            realMonth = month;         // 1900–1999
        } else if (month >= 21 && month <= 32) {
            realMonth = month - 20;    // 2000–2099
        } else if (month >= 41 && month <= 52) {
            realMonth = month - 40;    // 1800–1899
        } else {
            return false;
        }

        // Day must be in range 1..31 (coarse check — exact days-per-month not required)
        if (day < 1 || day > 31) {
            return false;
        }

        // Months with max 30 days
        if ((realMonth == 4 || realMonth == 6 || realMonth == 9 || realMonth == 11) && day > 30) {
            return false;
        }

        // February: accept up to 29 (leap year calculation is approximate for YY without century)
        if (realMonth == 2 && day > 29) {
            return false;
        }

        return true;
    }
}
