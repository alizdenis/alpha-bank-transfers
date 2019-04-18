package com.bank.alpha.processor.sync;

import com.bank.alpha.model.Account;
import com.bank.alpha.model.Transfer;
import com.bank.alpha.processor.TransferProcessStrategy;
import com.bank.alpha.repository.AccountRepository;
import com.bank.alpha.repository.TransferRepository;
import com.bank.alpha.validation.TransferValidation;
import com.bank.alpha.validation.ValidationException;
import io.reactivex.Single;

import java.math.BigDecimal;

public class TransferProcessSync implements TransferProcessStrategy {

    private final AccountRepository accountRepository;

    private final TransferRepository repository;

    private final TransferValidation validation;

    public TransferProcessSync(TransferRepository repository,
                               TransferValidation validation,
                               AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
        this.repository = repository;
        this.validation = validation;
    }

    @Override
    public void init() {
    }

    @Override
    public Single<Transfer> process(Transfer transfer) {
        return Single.just(transfer)
            .flatMap(validation::validateAndReturnTransfer)
            .flatMap(trans -> accountRepository.get(trans.getSourceId())
                .zipWith(accountRepository.get(trans.getDestinationId()), (source, destination) ->
                    Single.just(createTransferTx(transfer, source, destination))
                        .flatMap(this::checkIfSourceBalanceIsValid)
                        .flatMap(accountRepository::commitTransferTx)
                        .map(account -> trans)
                        .map(Transfer::toComplete)
                        .onErrorReturnItem(trans.toFailed())
                        .flatMap(repository::save)))
            .flatMap(transferSingle -> transferSingle);
    }

    private Single<TransferTx> checkIfSourceBalanceIsValid(TransferTx transferTx) {
        if (transferTx.getSource().getBalance().compareTo(BigDecimal.ZERO) < 0) {
            return Single.error(new ValidationException("Balance is too low"));
        }
        return Single.just(transferTx);
    }

    private TransferTx createTransferTx(Transfer transfer, Account source, Account destination) {
        return new TransferTx(
            source.subtractBalance(transfer.getAmount()),
            destination.addBalance(transfer.getAmount())
        );
    }
}
