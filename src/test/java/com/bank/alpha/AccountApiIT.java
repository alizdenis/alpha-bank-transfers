package com.bank.alpha;

import io.restassured.RestAssured;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(VertxExtension.class)
class AccountApiIT {

    @BeforeAll
    static void configureRestAssured() {
        RestAssured.baseURI = "http://localhost/api";
        RestAssured.port = 8080;
    }

    @BeforeEach
    void redeployVerticle(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new MainRxVerticle(), testContext.succeeding(id -> testContext.completeNow()));
    }

    @AfterAll
    static void unconfigureRestAssured() {
        RestAssured.reset();
    }

    @Test
    @DisplayName("should retrieve all accounts")
    void getAllAccounts() {
        when()
            .get("/account")
        .then()
            .statusCode(200)
            .body(
                "id", hasItems(1, 2, 3),
                "version", hasItems(1, 1, 1),
                "balance", hasItems(1000, 1000, 1000),
                "reservedAmount", hasItems(0, 0, 0)
            );
    }

    @Test
    @DisplayName("should retrieve 1 account data")
    void getOneAccount() {
        when()
            .get("/account/1")
        .then()
            .statusCode(200)
            .body(
                "id", equalTo(1),
                "version", equalTo(1),
                "balance", equalTo(1000),
                "reservedAmount", equalTo(0)
            );
    }

    @Test
    @DisplayName("should fail if account doesnt exists")
    void failOnNotExistingAccount() {
        String message =
            when()
                .get("/account/12")
            .then()
                .statusCode(404)
                .extract().body().asString();

        assertEquals("Not found!", message);
    }

    @Test
    @DisplayName("should fail if parsing id fails")
    void failOnParsingBadId() {
        String message =
            when()
                .get("/account/xx")
            .then()
                .statusCode(400)
                .extract().body().asString();

        assertEquals("Failed to parse id!", message);
    }

    @Test
    @DisplayName("should create new account")
    void createNewAccount() {
        given()
            .body("{\n" +
                  "  \"balance\" : 32131.12\n" +
                  "}")
        .when()
            .post("account")
        .then()
            .statusCode(201)
            .body(
                "id", notNullValue(),
                "version", equalTo(1),
                "balance", equalTo(32131.12f),
                "reservedAmount", equalTo(0)
            );
    }

    @Test
    @DisplayName("should fail if request is not valid")
    void accountCreateRequestNotValid() {
        String message =
            given()
                .body("{\n" +
                     "  \"balance\" : xx.12\n" +
                     "}")
            .when()
                .post("account")
            .then()
                .statusCode(400)
                .extract().body().asString();

        assertEquals("Failed to parse request!", message);
    }

    @Test
    @DisplayName("should fail if balance below 0")
    void accountCreateRequestBalance() {
        String message =
            given()
                .body("{\n" +
                     "  \"balance\" : -100\n" +
                     "}")
            .when()
                .post("account")
            .then()
                .statusCode(400)
                .extract().body().asString();

        assertEquals("Balance can not be lower than 0", message);
    }

    @Test
    @DisplayName("should fail transfer request is empty")
    void accountCreateWithRequestEmpty() {
        given()
            .body("{\n" +
                  "}")
        .when()
            .post("account")
        .then()
            .statusCode(400)
            .extract().body().asString();
    }

    @Test
    @DisplayName("should update account balance")
    void updateAccountBalance() {
        given()
            .body("{\n" +
                     "  \"balance\" : 7000\n" +
                     "}")
        .when()
            .put("account/1")
        .then()
            .statusCode(200)
            .body(
                "id", equalTo(1),
            "balance", equalTo(7000),
                "version", equalTo(2));
    }

    @Test
    @DisplayName("should fail updating account balance to negative")
    void updateAccountBalanceNegativeFail() {
        String message =
            given()
                .body("{\n" +
                      "  \"balance\" : -7000\n" +
                      "}")
            .when()
                .put("account/1")
            .then()
                .statusCode(400)
                .extract().body().asString();

        assertEquals("Balance can not be lower than 0", message);
    }
}