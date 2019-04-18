package com.bank.alpha;

import com.bank.alpha.config.DependencyConfig;
import com.bank.alpha.controller.AccountController;
import com.bank.alpha.controller.Controller;
import com.bank.alpha.controller.TransferController;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;

import java.util.ArrayList;
import java.util.List;


public class MainRxVerticle extends AbstractVerticle {

    private final List<Controller> controllers = new ArrayList<>();

    @Override
    public void start(Future<Void> startFuture) {
        configureDependencies();
        configJackson();

        vertx.createHttpServer().requestHandler(configRouter())
            .rxListen(8080)
            .subscribe(success -> {
                startFuture.complete();
                controllers.forEach(Controller::init);
            }, startFuture::fail);
    }

    private void configureDependencies() {
        Injector injector = Guice.createInjector(new DependencyConfig());
        controllers.add(injector.getInstance(AccountController.class));
        controllers.add(injector.getInstance(TransferController.class));
    }

    private void configJackson() {
        Json.prettyMapper.registerModule(new JavaTimeModule());
    }

    private Router configRouter() {
        Router router = Router.router(vertx);
        router.get("/").handler(event -> event.response()
                .setStatusCode(200)
                .end("Alpha bank transfers!"));

        controllers.forEach(controller ->
            router.mountSubRouter(controller.getPath(), controller.getRouter(vertx)));

        return router;
    }
}
