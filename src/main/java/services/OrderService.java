package services;

import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import models.Order;

import static io.restassured.RestAssured.given;

public class OrderService {
    private static final String URL = "https://qa-scooter.education-services.ru";

    private static final RestAssuredConfig CONFIG = RestAssuredConfig.config()
            .httpClient(HttpClientConfig.httpClientConfig()
                    .setParam("http.connection.timeout", 5000)
                    .setParam("http.socket.timeout", 5000));

    @Step("Создание заказа")
    public static Response createOrder(Order order) {
        return given()
                .config(CONFIG)
                .filter(new AllureRestAssured())
                .baseUri(URL)
                .contentType(ContentType.JSON)
                .and()
                .body(order)
                .when()
                .post("/api/v1/orders")
                .andReturn();
    }
    @Step("Получение списка заказов")
    public static Response getOrderList() {
        return given()
                .config(CONFIG)
                .filter(new AllureRestAssured())
                .baseUri(URL)
                .contentType(ContentType.JSON)
                .get("/api/v1/orders")
                .andReturn();
        }

}
