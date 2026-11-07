package services;

import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import models.Courier;
import models.CourierLogin;

import static io.restassured.RestAssured.given;

public class CourierService {
    private static final String URL = "https://qa-scooter.education-services.ru";

    // Добавлен таймаут на случай, чтобы тесты не висели бесконечно
    private static final RestAssuredConfig CONFIG = RestAssuredConfig.config()
            .httpClient(HttpClientConfig.httpClientConfig()
                    .setParam("http.connection.timeout", 5000)
                    .setParam("http.socket.timeout", 5000));

    @Step("Создать курьера")
    public static Response createCourier(Courier courier) {
        return given()
                .config(CONFIG)
                .filter(new AllureRestAssured())
                .baseUri(URL)
                .contentType(ContentType.JSON)
                .and()
                .body(courier)
                .when()
                .post("/api/v1/courier")
                .andReturn();
    }

    @Step("Авторизовать курьера")
    public static Response loginCourier(CourierLogin courierLogin) {
        return given()
                .config(CONFIG)
                .filter(new AllureRestAssured())
                .baseUri(URL)
                .contentType(ContentType.JSON)
                .and()
                .body(courierLogin)
                .when()
                .post("/api/v1/courier/login")
                .andReturn();
    }

    @Step("Удалить курьера по id")
    public static Response deleteCourier(int id) {
        return given()
                .config(CONFIG)
                .filter(new AllureRestAssured())
                .baseUri(URL)
                .when()
                .delete("/api/v1/courier/" + id)
                .andReturn();
    }

    public static Integer getCourierId(Courier courier) {
        Response response = loginCourier(new CourierLogin(courier.getLogin(), courier.getPassword()));
        if (response.statusCode() == 200) {
            return response.jsonPath().getInt("id");
        }
        return null;
    }
}
