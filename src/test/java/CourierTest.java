import io.restassured.response.Response;
import models.Courier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import services.CourierService;

import java.util.UUID;

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
            CourierService.deleteCourier(testCourier.getLogin());
        }
    }

    @Test
    @DisplayName("Курьера можно создать")
    public void createCourierSuccessfully() {
            Response response = CourierService.createCourier(testCourier);
            response.then().statusCode(201);
            boolean isOk = response.jsonPath().getBoolean("ok");
            assertTrue(isOk, "Ответ должен содержать ok: true");
        }
}
