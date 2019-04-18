package com.bank.alpha.repository;

import com.bank.alpha.model.Transfer;
import io.reactivex.Observable;
import io.reactivex.Single;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TransferRepository {

    private final AtomicInteger sequence = new AtomicInteger(1);

    private final ConcurrentHashMap<Integer, Transfer> transfers = new ConcurrentHashMap<>();

    public Single<Transfer> save(Transfer transfer) {
        return Single.just(transfer)
            .map(tr -> tr.toBuilder()
                .id(sequence.getAndIncrement())
                .startDate(OffsetDateTime.now(ZoneOffset.UTC))
                .lastUpdateDate(OffsetDateTime.now(ZoneOffset.UTC))
                .build())
            .doOnSuccess(tr -> transfers.put(tr.getId(), tr));
    }

    public Observable<Transfer> getAll() {
        return Observable.fromIterable(transfers.values());
    }

    public Single<Transfer> get(Integer id) {
        return Observable.fromIterable(transfers.values())
            .filter(transfer -> transfer.getId().equals(id))
            .firstOrError();
    }

    public Single<Transfer> update(Transfer transfer) {
        return Single.just(transfer)
            .map(tr -> {
                transfers.put(tr.getId(), tr.toBuilder()
                    .lastUpdateDate(OffsetDateTime.now(ZoneOffset.UTC))
                    .build());
                return tr;
            });
    }
}
