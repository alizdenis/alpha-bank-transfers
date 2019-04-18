package com.bank.alpha.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class Account {

    private final Integer id;

    private final int version;

    private final BigDecimal balance;

    private final BigDecimal reservedAmount;

    public boolean isSameVersion(Account account) {
        return version == account.getVersion();
    }

    public Account addBalance(BigDecimal amount) {
        return this.toBuilder()
            .balance(balance.add(amount))
            .build();
    }

    public Account subtractBalance(BigDecimal amount) {
        return this.toBuilder()
            .balance(balance.subtract(amount))
            .build();
    }

    public Account subtractBalanceWithReserve(BigDecimal amount) {
        return this.toBuilder()
            .balance(balance.subtract(amount))
            .reservedAmount(reservedAmount.add(amount))
            .build();
    }

    public Account addBalanceWithReserve(BigDecimal amount) {
        return this.toBuilder()
            .balance(balance.add(amount))
            .reservedAmount(reservedAmount.subtract(amount))
            .build();
    }

    public Account subtractReserve(BigDecimal amount) {
        return this.toBuilder()
            .reservedAmount(reservedAmount.subtract(amount))
            .build();
    }

    public Account incrementVersion() {
        return this.toBuilder()
            .version(version + 1)
            .build();
    }
}

