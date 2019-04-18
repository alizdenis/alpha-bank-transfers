package com.bank.alpha.controller.dto;

import com.bank.alpha.model.Account;
import lombok.Data;

import java.math.BigDecimal;

import static com.bank.alpha.controller.dto.RequestHelper.isBelowZero;
import static com.bank.alpha.controller.dto.RequestHelper.isNotNull;

@Data
public class AccountRequest {

    private BigDecimal balance;

    public Account toAccount() {
        validateAttributes();
        validateBalance();

        return Account.builder()
            .balance(balance)
            .build();
    }

    private void validateAttributes() {
        isNotNull(balance, "balance cannot be null");
    }

    private void validateBalance() {
        isBelowZero(balance, "Balance can not be lower than 0");
    }

}
