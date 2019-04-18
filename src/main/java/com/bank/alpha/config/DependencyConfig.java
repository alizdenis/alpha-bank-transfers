package com.bank.alpha.config;

import com.bank.alpha.controller.AccountController;
import com.bank.alpha.controller.TransferController;
import com.bank.alpha.processor.async.TransferProcessAsync;
import com.bank.alpha.processor.sync.TransferProcessSync;
import com.bank.alpha.processor.TransferProcessor;
import com.bank.alpha.repository.AccountRepository;
import com.bank.alpha.repository.TransferRepository;
import com.bank.alpha.validation.TransferValidation;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class DependencyConfig extends AbstractModule {

    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    public AccountController accountController(AccountRepository accountRepository) {
        return new AccountController(accountRepository);
    }

    @Provides
    @Singleton
    public AccountRepository accountRepository() {
        return new AccountRepository();
    }

    @Provides
    @Singleton
    public TransferController transferController(TransferRepository transferRepository,
                                                 TransferProcessor transferProcessor) {
        return new TransferController(transferRepository, transferProcessor);
    }

    @Provides
    @Singleton
    public TransferProcessor transferProcessor(TransferProcessAsync transferProcessAsync,
                                               TransferProcessSync transferProcessSync) {
        return new TransferProcessor(transferProcessSync, transferProcessAsync);
    }

    @Provides
    @Singleton
    public TransferProcessAsync transferProcessAsync(TransferRepository transferRepository,
                                                        TransferValidation transferValidation,
                                                        AccountRepository accountRepository) {
        return new TransferProcessAsync(transferRepository, transferValidation, accountRepository);
    }

    @Provides
    @Singleton
    public TransferProcessSync transferProcessSync(TransferRepository transferRepository,
                                                 TransferValidation transferValidation,
                                                 AccountRepository accountRepository) {
        return new TransferProcessSync(transferRepository, transferValidation, accountRepository);
    }

    @Provides
    @Singleton
    public TransferValidation transferValidation(AccountRepository accountRepository) {
        return new TransferValidation(accountRepository);
    }

    @Provides
    @Singleton
    public TransferRepository transferRepository() {
        return new TransferRepository();
    }

}
