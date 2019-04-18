package com.bank.alpha.processor.sync;

import com.bank.alpha.model.Account;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TransferTx {

    private Account source;
    private Account destination;

}
