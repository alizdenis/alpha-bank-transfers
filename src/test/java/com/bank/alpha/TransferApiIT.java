package com.bank.alpha;

import io.restassured.RestAssured;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
class TransferApiIT {

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
    @DisplayName("should post sync transfer and complete it")
    void postTransferSyncToComplete() {
        given()
            .body("{\n" +
                  "  \"sourceId\": 1,\n" +
                  "  \"destinationId\": 2,\n" +
                  "  \"amount\": 5\n" +
                  "}")
        .when()
            .post("transfer/sync")
        .then()
            .statusCode(201)
            .body(
                "id", notNullValue(),
                "sourceId", equalTo(1),
                "destinationId", equalTo(2),
                "amount", equalTo(5),
                "status", equalTo("COMPLETED"),
                "startDate", notNullValue(),
                "lastUpdateDate", notNullValue()
            );

        checkTransfer("COMPLETED");
        checkAccountBalance(1,995);
        checkAccountBalance(2,1005);
    }

    @Test
    @DisplayName("should post async transfer and complete it")
    void postTransferAsyncToComplete() {
        given()
            .body("{\n" +
                  "  \"sourceId\": 1,\n" +
                  "  \"destinationId\": 2,\n" +
                  "  \"amount\": 5\n" +
                  "}")
        .when()
            .post("transfer/async")
        .then()
            .statusCode(201)
            .body(
                "id", notNullValue(),
                "sourceId", equalTo(1),
                "destinationId", equalTo(2),
                "amount", equalTo(5),
                "status", equalTo("PENDING"),
                "startDate", notNullValue(),
                "lastUpdateDate", notNullValue()
            );

        sleep();
        checkTransfer("COMPLETED");
        checkAccountBalance(1,995);
        checkAccountBalance(2,1005);
    }

    @Test
    @DisplayName("should post async transfer to non existing account")
    void postTransferSyncToNotExistingAccount() {
        given()
            .body("{\n" +
                  "  \"sourceId\": 1,\n" +
                  "  \"destinationId\": 7,\n" +
                  "  \"amount\": 5\n" +
                  "}")
        .when()
            .post("transfer/sync")
        .then()
            .statusCode(404);

        checkAccountBalance(1,1000);
        checkAccountBalance(2,1000);
    }

    @Test
    @DisplayName("should post async transfer to non existing account")
    void postTransferAsyncToNotExistingAccount() {
        given()
            .body("{\n" +
                  "  \"sourceId\": 1,\n" +
                  "  \"destinationId\": 7,\n" +
                  "  \"amount\": 5\n" +
                  "}")
        .when()
            .post("transfer/async")
        .then()
            .statusCode(201)
            .body(
                "id", notNullValue(),
                "sourceId", equalTo(1),
                "destinationId", equalTo(7),
                "amount", equalTo(5),
                "status", equalTo("PENDING"),
                "startDate", notNullValue(),
                "lastUpdateDate", notNullValue()
            );

        sleep();
        checkTransfer("FAILED");
        checkAccountBalance(1,1000);
        checkAccountBalance(2,1000);
    }

    private void checkTransfer(String status) {
        given()
            .pathParam("id", 1)
        .when()
            .get("transfer/{id}")
        .then()
            .statusCode(200)
            .body(
                "status", equalTo(status),
                "lastUpdateDate", notNullValue()
            );
    }

    private void checkAccountBalance(int id, int balance) {
        given()
            .pathParam("id", id)
        .when()
            .get("account/{id}")
        .then()
            .statusCode(200)
            .body(
                "balance", equalTo(balance),
                "reservedAmount", equalTo(0)
            );
    }

    @ParameterizedTest
    @ValueSource(strings = {"async", "sync"})
    @DisplayName("should fail if transfer amount is below 0")
    void postTransferFailWithNegativeAmount(String apiType) {
        String message =
            given()
                .pathParam("apiType", apiType)
                .body("{\n" +
                      "  \"sourceId\": 1,\n" +
                      "  \"destinationId\": 2,\n" +
                      "  \"amount\": -1\n" +
                      "}")
            .when()
                .post("transfer/{apiType}")
            .then()
                .statusCode(400)
                .extract().asString();
        assertEquals("Amount can not be lower or equal to 0",  message);
    }

    @ParameterizedTest
    @ValueSource(strings = {"async", "sync"})
    @DisplayName("should fail if transfer source id is not valid")
    void postTransferFailWithBadSourceId(String apiType) {
        String message =
            given()
                .pathParam("apiType", apiType)
                .body("{\n" +
                      "  \"sourceId\": 0,\n" +
                      "  \"destinationId\": 2,\n" +
                      "  \"amount\": 123\n" +
                      "}")
            .when()
                .post("transfer/{apiType}")
            .then()
                .statusCode(400)
                .extract().asString();
        assertEquals("Source id is not valid",  message);
    }

    @ParameterizedTest
    @ValueSource(strings = {"async", "sync"})
    @DisplayName("should fail if transfer destination id is not valid")
    void postTransferFailWithBadDestinationId(String apiType) {
        String message =
            given()
                .pathParam("apiType", apiType)
                .body("{\n" +
                      "  \"sourceId\": 1,\n" +
                      "  \"destinationId\": 0,\n" +
                      "  \"amount\": 123\n" +
                      "}")
            .when()
                .post("transfer/{apiType}")
            .then()
                .statusCode(400)
                .extract().asString();
        assertEquals("Destination id is not valid",  message);
    }

    @ParameterizedTest
    @ValueSource(strings = {"async", "sync"})
    @DisplayName("should fail if transfer source and destination is the same")
    void postTransferFailWithSameSourceAndDestinationIds(String apiType) {
        String message =
            given()
                .pathParam("apiType", apiType)
                .body("{\n" +
                      "  \"sourceId\": 1,\n" +
                      "  \"destinationId\": 1,\n" +
                      "  \"amount\": 123\n" +
                      "}")
            .when()
                .post("transfer/{apiType}")
            .then()
                .statusCode(400)
                .extract().asString();
        assertEquals("Source and destination cannot be the same",  message);
    }

    @ParameterizedTest
    @ValueSource(strings = {"async", "sync"})
    @DisplayName("should fail if transfer source and destination is the same")
    void postTransferWithBadRequest(String apiType) {
        String message =
            given()
                .pathParam("apiType", apiType)
                .body("{\n" +
                      "  \"sourceId\": 1,\n" +
                      "  \"destinationId\": 1X,\n" +
                      "  \"amount\": 123\n" +
                      "}")
            .when()
                .post("transfer/{apiType}")
            .then()
                .statusCode(400)
                .extract().asString();
        assertEquals("Failed to parse request!",  message);
    }

    @ParameterizedTest
    @ValueSource(strings = {"async", "sync"})
    @DisplayName("should fail transfer request is empty")
    void postTransferWithEmptyValues(String apiType) {
        given()
            .pathParam("apiType", apiType)
            .body("{\n" +
                  "}")
        .when()
            .post("transfer/{apiType}")
        .then()
            .statusCode(400);
    }



    @Test
    @DisplayName("should get all transfers")
    void getAllTransfers() {
        Integer idA =
            given()
                .body("{\n" +
                      "  \"sourceId\": 1,\n" +
                      "  \"destinationId\": 2,\n" +
                      "  \"amount\": 5\n" +
                      "}")
            .when()
                .post("transfer/async")
            .then()
                .statusCode(201)
                .extract().body().jsonPath().get("id");

        Integer idB =
            given()
                .body("{\n" +
                      "  \"sourceId\": 3,\n" +
                      "  \"destinationId\": 2,\n" +
                      "  \"amount\": 55\n" +
                      "}")
            .when()
                .post("transfer/async")
            .then()
                .statusCode(201)
                .extract().body().jsonPath().get("id");

        when()
            .get("transfer")
        .then()
            .statusCode(200)
            .body(
                "id", hasItems(idA, idB),
                "sourceId", hasItems(1, 3),
                "destinationId", hasItems(2, 2),
                "amount", hasItems(5, 55),
                "status", hasItems("PENDING", "PENDING"),
                "startDate", notNullValue()
            );
    }

    @Test
    @DisplayName("should get transfer by its id")
    void getTransferById() {
        Integer id =
            given()
                .body("{\n" +
                      "  \"sourceId\": 1,\n" +
                      "  \"destinationId\": 2,\n" +
                      "  \"amount\": 5\n" +
                      "}")
            .when()
                .post("transfer/async")
            .then()
                .statusCode(201)
                .extract().body().jsonPath().get("id");

        given()
            .pathParam("id", id)
        .when()
            .get("transfer/{id}")
        .then()
            .statusCode(200)
            .body(
                "id", equalTo(1),
                "sourceId", equalTo(1),
                "destinationId", equalTo(2),
                "amount", equalTo(5),
                "status", equalTo("PENDING"),
                "startDate", notNullValue()
            );
    }

    @Test
    @DisplayName("should fail if parsing id fails")
    void failOnParsingBadId() {
        String message =
            when()
                .get("transfer/xx")
                .then()
                .statusCode(400)
                .extract().body().asString();

        assertEquals("Failed to parse id!", message);
    }

    private void sleep() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}