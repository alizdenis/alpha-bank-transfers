package com.bank.alpha.controller.dto;

import com.bank.alpha.model.Transfer;
import com.bank.alpha.validation.ValidationException;
import lombok.Data;

import java.math.BigDecimal;

import static com.bank.alpha.controller.dto.RequestHelper.isNotNull;

@Data
public class TransferRequest {

    private Integer sourceId;
    private Integer destinationId;
    private BigDecimal amount;

    public Transfer toTransfer() {
        validateAttributes();
        validateAmount();
        validateSourceAndDestinationIds();
        validateSourceAndDestinationIsNotTheSame();

        return Transfer.builder()
            .sourceId(sourceId)
            .destinationId(destinationId)
            .amount(amount)
            .build();
    }

    private void validateAttributes() {
        isNotNull(sourceId, "Source cannot be null");
        isNotNull(destinationId, "Destination cannot be null");
        isNotNull(amount, "amount cannot be null");
    }

    private void validateAmount() {
        if (amount.compareTo(BigDecimal.ZERO) < 1) {
            throw new ValidationException("Amount can not be lower or equal to 0");
        }
    }

    private void validateSourceAndDestinationIds() {
        if (sourceId < 1) {
            throw new ValidationException("Source id is not valid");
        } else if (destinationId < 1) {
            throw new ValidationException("Destination id is not valid");
        }
    }

    private void validateSourceAndDestinationIsNotTheSame() {
        if (sourceId.equals(destinationId)) {
            throw new ValidationException("Source and destination cannot be the same");
        }
    }
}
