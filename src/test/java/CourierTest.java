import io.restassured.response.Response;
import models.Courier;
import models.CourierLogin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import services.CourierService;

import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CourierTest {

    private Courier testCourier;

    @BeforeEach
    public void setUp() {
        String uniqueLogin = "login_" + UUID.randomUUID().toString().substring(0, 8);
        testCourier = new Courier(uniqueLogin, "password123", "Иван");
    }

    @AfterEach
    public void tearDown() {
        if (testCourier != null) {
            Integer id = CourierService.getCourierId(testCourier);
            if (id != null) {
                CourierService.deleteCourier(id);
            }
        }
    }

    // Метод для тестов, где курьер уже должен существовать
    private void createTestCourier() {
        CourierService.createCourier(testCourier).then().statusCode(201);
    }

    // Тесты на создание курьера

    @Test
    @DisplayName("Курьера можно создать и возвращается код 201")
    public void createCourierSuccessfully() {
        Response response = CourierService.createCourier(testCourier);
        response.then().statusCode(201).body("ok", equalTo(true));
    }

    @Test
    @DisplayName("Нельзя создать двух одинаковых курьеров и возвращается код 409")
    public void createDuplicateCourierIsImpossible() {
        createTestCourier();
        Response secondResponse = CourierService.createCourier(testCourier);
        secondResponse.then().statusCode(409).body("message", equalTo("Этот логин уже используется. Попробуйте другой."));
    }

    @Test
    @DisplayName("Нельзя создать курьера с уже существующим логином и возвращается код 409")
    public void createCourierWithExistingLoginReturns409() {
        createTestCourier();
        Courier sameLogin = new Courier(testCourier.getLogin(), "otherPassword", "Пётр");
        Response response = CourierService.createCourier(sameLogin);
        response.then().statusCode(409).body("message", equalTo("Этот логин уже используется. Попробуйте другой."));
    }

    // Метод для генерации тестовых данных для проверки обязательности полей при создании курьера
    static Stream<Courier> couriersWithoutRequiredField() {
        String uniqueLogin = "login_" + UUID.randomUUID();
        return Stream.of(
                new Courier(uniqueLogin, "", "Иван"),
                new Courier(uniqueLogin, null, "Иван"),
                new Courier(null, "password123", ""),
                new Courier("", "password123", "Семён")
        );
    }

    @ParameterizedTest
    @DisplayName("Нельзя создать курьера без обязательных полей и возвращается код 400")
    @MethodSource("couriersWithoutRequiredField")
    public void createCourierWithoutRequiredFieldReturns400(Courier incompleteCourier) {
        Response response = CourierService.createCourier(incompleteCourier);
        response.then().statusCode(400).body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    // Тесты на логин курьера

    @Test
    @DisplayName("Курьер может авторизоваться и возвращается код 200")
    public void loginSuccessfully() {
        createTestCourier();
        CourierLogin loginData = new CourierLogin(testCourier.getLogin(), testCourier.getPassword());
        Response response = CourierService.loginCourier(loginData);
        response.then().statusCode(200);
        int courierId = response.jsonPath().getInt("id");
        assertTrue(courierId > 0, "Ответ должен содержать id курьера");
    }

    //Метод для генерации тестовых данных для проверки обязательности полей при входе в систему
    static Stream<Arguments> couriersWithoutRequiredFieldForLogin() {
        return Stream.of(
                Arguments.of("без login", new CourierLogin(null, "1234")),
                Arguments.of("без password", new CourierLogin("login", null))
        );
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("Для авторизации нужно передать все обязательные поля")
    @MethodSource("couriersWithoutRequiredFieldForLogin")
    public void loginWithoutRequiredFieldsReturns400(String caseName, CourierLogin incompleteCourier) {
        Response response = CourierService.loginCourier(incompleteCourier);
        response.then().statusCode(400).body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Система вернёт ошибку 404, если неправильно указать пароль")
    public void loginWithWrongPasswordReturnsError() {
        createTestCourier();
        CourierLogin wrongPassword = new CourierLogin(testCourier.getLogin(), "374856sgfuydtgsug");
        Response response = CourierService.loginCourier(wrongPassword);
        response.then().statusCode(404).body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Система вернёт ошибку 404, если неправильно указать логин")
    public void loginWithWrongLoginReturnsError() {
        createTestCourier();
        CourierLogin wrongLogin = new CourierLogin("wrong_" + testCourier.getLogin(), testCourier.getPassword());
        Response response = CourierService.loginCourier(wrongLogin);
        response.then().statusCode(404).body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Если авторизоваться под несуществующим пользователем, запрос возвращает ошибку")
    public void loginWithNonExistentUserReturns404() {
        CourierLogin nonExistedLogin = new CourierLogin("login_" + UUID.randomUUID(), "password");
        Response response = CourierService.loginCourier(nonExistedLogin);
        response.then().statusCode(404).body("message", equalTo("Учетная запись не найдена"));
    }
}
