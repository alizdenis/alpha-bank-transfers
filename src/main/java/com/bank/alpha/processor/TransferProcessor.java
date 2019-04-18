package com.bank.alpha.processor;

import com.bank.alpha.model.Transfer;
import io.reactivex.Single;

public class TransferProcessor {

    private final TransferProcessStrategy asyncProcess;

    private final TransferProcessStrategy syncProcess;

    public TransferProcessor(TransferProcessStrategy syncProcess,
                             TransferProcessStrategy asyncProcess) {
        this.syncProcess = syncProcess;
        this.asyncProcess = asyncProcess;
    }

    public void init() {
        syncProcess.init();
        asyncProcess.init();
    }

    public Single<Transfer> processSync(Transfer transfer) {
        return syncProcess.process(transfer);
    }

    public Single<Transfer> processAsync(Transfer transfer) {
        return asyncProcess.process(transfer);
    }

}
