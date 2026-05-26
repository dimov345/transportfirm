package com.example.transportfirm.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Shared financial constants for currency conversion.
 * Bulgaria's fixed BGN → EUR rate as per the EU accession treaty.
 */
public final class FinancialConstants {

    /** 1 EUR = 1.95583 BGN (fixed EU rate) */
    public static final BigDecimal EUR_TO_BGN = new BigDecimal("1.95583");

    /** 1 BGN = 0.511292... EUR */
    public static final BigDecimal BGN_TO_EUR = BigDecimal.ONE
            .divide(EUR_TO_BGN, 6, RoundingMode.HALF_UP);

    /** Weekly salary factor: monthly ÷ 4.33 (average weeks per month) */
    public static final BigDecimal WEEKS_PER_MONTH = new BigDecimal("4.33");

    private FinancialConstants() {}
}
