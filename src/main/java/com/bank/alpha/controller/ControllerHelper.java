package com.bank.alpha.controller;

import com.bank.alpha.validation.ValidationException;
import io.reactivex.Single;
import io.reactivex.functions.BiConsumer;
import io.vertx.core.json.Json;
import io.vertx.reactivex.ext.web.RoutingContext;

import java.util.NoSuchElementException;
import java.util.Optional;

class ControllerHelper {

    static <T> BiConsumer<T, Throwable> ok(RoutingContext context) {
        return writeJsonResponse(context, 200);
    }

    static <T> BiConsumer<T, Throwable> created(RoutingContext context) {
        return writeJsonResponse(context, 201);
    }

    private static <T> BiConsumer<T, Throwable> writeJsonResponse(RoutingContext context, int status) {
        return (res, err) -> {
            if (err != null) {
                if (err instanceof NoSuchElementException) {
                    context.response().setStatusCode(404).end("Not found!");
                } else if (err instanceof ValidationException) {
                    context.response().setStatusCode(400).end(
                        Optional.ofNullable(err.getMessage())
                            .orElse("Bad request!"));
                } else {
                    context.fail(err);
                }
            } else {
                context.response().setStatusCode(status)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(res));
            }
        };
    }

    static <T> Single<T> parseRequestBody(RoutingContext context, Class<T> type) {
        try {
            T request = context.getBodyAsJson().mapTo(type);
            return Single.just(request);
        } catch (Exception exc) {
            return Single.error(new ValidationException("Failed to parse request!"));
        }
    }

    static Single<Integer> parseId(RoutingContext context) {
        try {
            Integer id = Integer.valueOf(context.pathParam("id"));
            return Single.just(id);
        } catch (Exception exc) {
            return Single.error(new ValidationException("Failed to parse id!"));
        }
    }
}
