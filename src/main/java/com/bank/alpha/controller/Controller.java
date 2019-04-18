package com.bank.alpha.controller;

import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.Router;

public interface Controller {

    void init();

    String getPath();

    Router getRouter(Vertx vertx);

}
