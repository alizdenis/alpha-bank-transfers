package com.bank.alpha.validation;

import com.bank.alpha.model.Account;
import com.bank.alpha.model.Transfer;
import com.bank.alpha.repository.AccountRepository;
import io.reactivex.Single;

import java.math.BigDecimal;

public class TransferValidation {

    private final AccountRepository accountRepository;

    public TransferValidation(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Single<Transfer> validateAndReturnTransfer(Transfer transfer) {
        return validateAndReturnAccount(transfer)
            .map(account -> transfer);
    }

    public Single<Account> validateAndReturnAccount(Transfer transfer) {
        return checkIfAccountExists(transfer)
            .flatMap(account -> checkAccountBalance(transfer, account));
    }

    private Single<Account> checkIfAccountExists(Transfer request) {
        return accountRepository.get(request.getSourceId());
    }

    private Single<Account> checkAccountBalance(Transfer transfer, Account account) {
        return Single.just(account)
            .flatMap(acc -> {
                if (account.getBalance().subtract(transfer.getAmount()).compareTo(BigDecimal.ZERO) < 0) {
                    return Single.error(new ValidationException("Account balance is to low"));
                }
                return Single.just(acc);
            });
    }
}
