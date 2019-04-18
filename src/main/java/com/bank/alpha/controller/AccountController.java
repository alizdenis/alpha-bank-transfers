package com.bank.alpha.controller;

import com.bank.alpha.controller.dto.AccountRequest;
import com.bank.alpha.model.Account;
import com.bank.alpha.repository.AccountRepository;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;

import java.math.BigDecimal;

import static com.bank.alpha.controller.ControllerHelper.*;

public class AccountController implements Controller {

    private final AccountRepository repository;

    public AccountController(AccountRepository repository) {
        this.repository = repository;
    }

    public String getPath() {
        return "/api/account";
    }

    public Router getRouter(Vertx vertx) {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.post("/").handler(this::createAccount);
        router.get("/").handler(this::getAllAccount);
        router.get("/:id").handler(this::getAccount);
        router.put("/:id").handler(this::updateAccount);
        return router;
    }

    public void init() {
        Observable.range(1, 3)
            .flatMapSingle(integer -> repository.save(Account.builder()
                .balance(BigDecimal.valueOf(1000))
                .build()))
            .subscribe();
    }

    private void getAllAccount(RoutingContext context) {
        Observable.just(context)
            .flatMap(rc -> repository.getAll())
            .toList()
            .subscribe(ok(context));
    }

    private void getAccount(RoutingContext context) {
        Single.just(context)
            .flatMap(ControllerHelper::parseId)
            .flatMap(repository::get)
            .subscribe(ok(context));
    }

    private void createAccount(RoutingContext context) {
        Single.just(context)
            .flatMap(ctx -> parseRequestBody(ctx, AccountRequest.class))
            .map(AccountRequest::toAccount)
            .flatMap(repository::save)
            .subscribe(created(context));
    }

    private void updateAccount(RoutingContext context) {
        Single.just(context)
            .flatMap(ControllerHelper::parseId)
            .flatMap(repository::get)
            .flatMap(account -> Single.just(account)
                .flatMap(acc -> parseRequestBody(context, AccountRequest.class))
                .map(AccountRequest::toAccount)
                .map(accountRequest -> account.toBuilder()
                    .balance(accountRequest.getBalance())
                    .build()))
            .flatMap(repository::commitAccount)
            .subscribe(ok(context));
    }

}
