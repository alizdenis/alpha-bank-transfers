package com.bank.alpha.controller;

import com.bank.alpha.controller.dto.TransferRequest;
import com.bank.alpha.repository.TransferRepository;
import com.bank.alpha.processor.TransferProcessor;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;

import static com.bank.alpha.controller.ControllerHelper.*;

public class TransferController implements Controller {

    private final TransferRepository repository;

    private final TransferProcessor processor;

    public TransferController(TransferRepository repository,
                              TransferProcessor processor) {
        this.repository = repository;
        this.processor = processor;
    }

    @Override
    public void init() {
        processor.init();
    }

    @Override
    public String getPath() {
        return "/api/transfer";
    }

    @Override
    public Router getRouter(Vertx vertx) {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.post("/sync").handler(this::postWholeTransfer);
        router.post("/async").handler(this::postPartialTransfer);
        router.get("/").handler(this::getAllTransfer);
        router.get("/:id").handler(this::getTransfer);
        return router;
    }

    private void getTransfer(RoutingContext context) {
        Single.just(context)
            .flatMap(ControllerHelper::parseId)
            .flatMap(repository::get)
            .subscribe(ok(context));
    }

    private void getAllTransfer(RoutingContext context) {
        Observable.just(context)
            .flatMap(rc -> repository.getAll())
            .toList()
            .subscribe(ok(context));
    }

    private void postWholeTransfer(RoutingContext context) {
        Single.just(context)
            .flatMap(ctx -> parseRequestBody(ctx, TransferRequest.class))
            .map(TransferRequest::toTransfer)
            .flatMap(processor::processSync)
            .subscribe(created(context));
    }

    private void postPartialTransfer(RoutingContext context) {
        Single.just(context)
            .flatMap(ctx -> parseRequestBody(ctx, TransferRequest.class))
            .map(TransferRequest::toTransfer)
            .flatMap(processor::processAsync)
            .subscribe(created(context));
    }
}
