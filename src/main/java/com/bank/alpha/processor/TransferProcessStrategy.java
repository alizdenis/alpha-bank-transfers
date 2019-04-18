package com.bank.alpha.processor;

import com.bank.alpha.model.Transfer;
import io.reactivex.Single;

public interface TransferProcessStrategy {

    void init();

    Single<Transfer> process(Transfer transfer);

}
