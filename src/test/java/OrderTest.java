import io.restassured.response.Response;
import models.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import services.OrderService;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class OrderTest {

    private static Order orderWithColor(List<String> color) {
        return new Order("Иван", "Петров", "Арбатская площадь, 14с1", "Арбатская", "+79155582267",
                1, "2026-11-08", "Комментарий для курьера", color);
    }

    // Метод для генерации тестовых данных по цветам
    static Stream<Arguments> orderColors() {
        return Stream.of(
                Arguments.of("один цвет: BLACK", List.of("BLACK")),
                Arguments.of("один цвет: GREY", List.of("GREY")),
                Arguments.of("оба цвета", List.of("BLACK", "GREY")),
                Arguments.of("без цвета", null)
        );
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("Заказ можно создать с любым вариантом цвета: код 201 и в ответе есть track")
    @MethodSource("orderColors")
    public void createOrderReturnsTrack(String caseName, List<String> color) {
        Response response = OrderService.createOrder(orderWithColor(color));
        response.then().statusCode(201).body("track", notNullValue());
    }

    @Test
    @DisplayName("Список заказов: код 200 и возвращается список заказов")
    public void getOrdersReturnsList() {
        Response response = OrderService.getOrderList();
        response.then().statusCode(200);
        List<Object> orders = response.jsonPath().getList("orders");
        assertNotNull(orders, "В ответе должен быть список заказов");
        assertFalse(orders.isEmpty(), "Список заказов не должен быть пустым");
    }
}
