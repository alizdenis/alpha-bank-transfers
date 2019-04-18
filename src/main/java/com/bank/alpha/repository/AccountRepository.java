package com.bank.alpha.repository;

import com.bank.alpha.model.Account;
import com.bank.alpha.processor.TransactionException;
import com.bank.alpha.processor.sync.TransferTx;
import io.reactivex.Observable;
import io.reactivex.Single;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AccountRepository {

    private final AtomicInteger sequence = new AtomicInteger(1);

    private final ConcurrentHashMap<Integer, Account> accounts = new ConcurrentHashMap<>();

    public Single<Account> save(Account account) {
        return Single.just(account)
            .map(acc -> {
                Account build = acc.toBuilder()
                    .id(sequence.getAndIncrement())
                    .version(1)
                    .reservedAmount(BigDecimal.ZERO)
                    .build();
                accounts.put(build.getId(), build);
                return build;
            });
    }

    public Observable<Account> getAll() {
        return Observable.fromIterable(accounts.values());
    }

    public Single<Account> get(Integer id) {
        return Observable.fromIterable(accounts.values())
            .filter(account -> account.getId().equals(id))
            .firstOrError();
    }

    public Single<Account> commitAccount(Account account) {
        return Single.just(account)
            .flatMap(acc -> {
                synchronized (accounts) {
                    Account existing = accounts.get(acc.getId());
                    if (existing.isSameVersion(acc)) {
                        Account incAccount = acc.incrementVersion();
                        accounts.put(acc.getId(), incAccount);
                        return Single.just(incAccount);
                    } else {
                        return Single.error(new TransactionException("Account update tx failed!"));
                    }
                }
            });
    }

    public Single<TransferTx> commitTransferTx(TransferTx transferTx) {
        return Single.just(transferTx)
            .flatMap(tx -> {
                synchronized (accounts) {
                    Account source = accounts.get(transferTx.getSource().getId());
                    Account destination = accounts.get(transferTx.getDestination().getId());

                    if (source.isSameVersion(transferTx.getSource()) &&
                        destination.isSameVersion(transferTx.getDestination())) {

                        accounts.put(source.getId(), transferTx.getSource().incrementVersion());
                        accounts.put(destination.getId(), transferTx.getDestination().incrementVersion());
                        return Single.just(transferTx);
                    } else {
                        return Single.error(new TransactionException("Accounts update tx failed!"));
                    }
                }
            });
    }

}