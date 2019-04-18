package com.bank.alpha.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static com.bank.alpha.model.Transfer.TransferStatus.*;

@Getter
@Builder(toBuilder = true)
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Transfer {

    public enum  TransferStatus {
        PENDING,
        RETRYING,
        COMPLETED,
        FAILED
    }

    private final Integer id;

    private final Integer sourceId;

    private final Integer destinationId;

    private final BigDecimal amount;

    private final TransferStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private final OffsetDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private final OffsetDateTime lastUpdateDate;

    public Transfer toPending() {
        return this.toBuilder()
            .status(PENDING)
            .build();
    }

    public Transfer toRetrying() {
        return this.toBuilder()
            .status(RETRYING)
            .build();
    }

    public Transfer toComplete() {
        return this.toBuilder()
            .status(COMPLETED)
            .build();
    }

    public Transfer toFailed() {
        return this.toBuilder()
            .status(FAILED)
            .build();
    }
}

