package com.bank.alpha.processor.async;

import com.bank.alpha.model.Transfer;
import com.bank.alpha.processor.TransactionException;
import com.bank.alpha.processor.TransferProcessStrategy;
import com.bank.alpha.repository.AccountRepository;
import com.bank.alpha.repository.TransferRepository;
import com.bank.alpha.validation.TransferValidation;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import java.util.concurrent.TimeUnit;

public class TransferProcessAsync implements TransferProcessStrategy {

    private final AccountRepository accountRepository;

    private final TransferRepository repository;

    private final TransferValidation validation;

    private final PublishSubject<MultiStepTx> txQueue = PublishSubject.create();

    private final PublishSubject<MultiStepTx> txRetryQueue = PublishSubject.create();

    public TransferProcessAsync(TransferRepository repository,
                                TransferValidation validation,
                                AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
        this.repository = repository;
        this.validation = validation;
    }

    @Override
    public void init() {
        txQueue
            .subscribeOn(Schedulers.computation())
            .flatMap(this::processTx)
            .subscribe(tx -> System.out.println("txQueue: " + tx), System.out::println);

        txRetryQueue
            .delay(200, TimeUnit.MILLISECONDS)
            .subscribe(tx -> {
                System.out.println("txRetryQueue + " + tx);
                txQueue.onNext(tx);
            });
    }

    @Override
    public Single<Transfer> process(Transfer transfer) {
        return Single.just(transfer)
            .flatMap(tr -> validation.validateAndReturnAccount(tr)
                .map(account -> account.subtractBalanceWithReserve(tr.getAmount()))
                .flatMap(accountRepository::commitAccount)
                .map(account -> tr))
            .map(Transfer::toPending)
            .flatMap(repository::save)
            .flatMap(trans -> Single.just(createMultiStepTx(trans))
                .doOnSuccess(txQueue::onNext)
                .map(multiStepTx -> trans));
    }

    private MultiStepTx createMultiStepTx(Transfer trans) {
        return MultiStepTx.builder()
                .parent(trans)
                .step(MultiStepTx.Step.builder()
                    .id(trans.getDestinationId())
                    .action(account -> account.addBalance(trans.getAmount()))
                    .build())
                .step(MultiStepTx.Step.builder()
                    .id(trans.getSourceId())
                    .action(account -> account.subtractReserve(trans.getAmount()))
                    .build())
                .onFail(MultiStepTx.Step.builder()
                    .id(trans.getSourceId())
                    .action(account -> account.addBalanceWithReserve(trans.getAmount()))
                    .build())
                .build();
    }

    private Observable<Transfer> processTx(MultiStepTx multiStepTx) {
        return Observable.just(multiStepTx)
            .flatMap(multiStep ->
                Observable.just(multiStep.getCurrentStep())
                    .flatMapSingle(stepTx -> accountRepository.get(stepTx.getId())
                        .map(account -> stepTx.getAction().apply(account))
                        .flatMap(accountRepository::commitAccount))
                .map(account -> multiStep.getParent())
                .map(transfer -> checkIfTxCompleted(multiStep, transfer))
                .onErrorReturn(throwable -> handleFailure(multiStep, throwable))
                .filter(this::isNotPendingTransferStatus)
                .flatMapSingle(repository::update));
    }

    private Transfer checkIfTxCompleted(MultiStepTx multiStep, Transfer transfer) {
        if (multiStep.isFailed()) {
            return transfer;
        } else if (multiStep.hasNextStep()) {
            txRetryQueue.onNext(multiStep.stepComplete());
            return transfer;
        }
        return transfer.toComplete();
    }

    private Transfer handleFailure(MultiStepTx multiStep, Throwable throwable) {
        if (throwable instanceof TransactionException) {
            txRetryQueue.onNext(multiStep);
            return multiStep.getParent().toRetrying();
        } else if (!multiStep.isFailed()) {
            txRetryQueue.onNext(multiStep.toFailed());
        }
        return multiStep.getParent().toFailed();
    }

    private boolean isNotPendingTransferStatus(Transfer transfer) {
        return transfer.getStatus() != Transfer.TransferStatus.PENDING;
    }
}
