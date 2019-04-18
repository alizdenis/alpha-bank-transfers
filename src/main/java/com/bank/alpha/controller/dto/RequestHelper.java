package com.bank.alpha.controller.dto;

import com.bank.alpha.validation.ValidationException;

import java.math.BigDecimal;

class RequestHelper {

    static void isNotNull(Object sourceId, String message) {
        if (sourceId == null) {
            throw new ValidationException(message);
        }
    }

    static void isBelowZero(BigDecimal decimal, String message) {
        if (decimal.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException(message);
        }
    }

}
