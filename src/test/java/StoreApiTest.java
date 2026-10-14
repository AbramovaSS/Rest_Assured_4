import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class StoreApiTest {

    RequestSpecification requestSpec = new RestApiBuilder().requestSpecification;

    @Tag("api")
    @Test
    @DisplayName("Запрос пустого списка товаров")
    void requestForListOfGoodsGiven() {
        given()
                .baseUri("http://localhost:8080")
                .log().all()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/goods/list")
                .then()
                .log().all()
                .statusCode(200)
                .body("goods", empty());

    }

    @Tag("api")
    @Test
    @DisplayName("Запрос пустого списка товаров с использованием RequestSpecification")
    void requestForListOfGoodsRequestSpec() {
        given()
                .spec(requestSpec)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .get("/goods/list")
                .then()
                .log().all()
                .statusCode(200)
                .body("goods", empty());
    }

    @Tag("api")
    @Test
    @DisplayName("Добавление товара с проверкой, что он вернулся в списке")
    void addAndVerifyProduct() {
        given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body("""
                        {
                           "name": "Кувшин",
                           "price": 100
                        }
                        """)
                .post("/goods/add")
                .then()
                .log().all()
                .statusCode(200);

        given()
                .spec(requestSpec)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .get("/goods/list")
                .then()
                .log().all()
                .statusCode(200)
                .body("goods.name", contains("Кувшин"));
    }

    @Tag("api")
    @Test
    @DisplayName("Добавление товара с проверкой, что он вернулся в списке через AssertJ")
    void addAndVerifyProductAssertJ() {
        given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body("""
                        {
                           "name": "Бокал",
                           "price": 300
                        }
                        """)
                .post("/goods/add")
                .then()
                .log().all()
                .statusCode(200);

        Response response = given()
                .spec(requestSpec)
                .get("/goods/list")
                .then()
                .spec(RestApiAssert.successResponseSpec)
                .extract().response();

        assertThat(response.jsonPath().getList("goods.name"))
                .as("Поле name должно содержать 'Бокал'")
                .contains("Бокал");
    }

    @Tag("api")
    @Test
    @DisplayName("Добавление товара с отрицательной ценой")
    void addProduct400() {
        Response response = given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body("""
                        {
                           "name": "Бокал",
                           "price": -1
                        }
                        """)
                .post("/goods/add")
                .then()
                .spec(RestApiAssert.errorResponseSpec)
                .extract().response();

        assertThat(response.statusCode())
                .as("Код ответа - 400")
                .isEqualTo(400);

        JsonPath jsonPath = response.jsonPath();

        assertThat(jsonPath.getString("price"))
                .as("Появляется сообщение об ошибке")
                .isEqualTo("Price can not be less, than 0");
    }

    @Tag("api")
    @Test
    @DisplayName("Получение товара по ID")
    void findProductById() {

        int expectedId = 1;
        String expectedName = "Кувшин";
        double expectedPrice = 100.0;

        Response response = given()
                .spec(requestSpec)
                .pathParam("id", expectedId)
                .get("/goods/{id}")
                .then()
                .spec(RestApiAssert.successResponseSpec)
                .extract().response();

        assertThat(response.statusCode())
                .as("Код ответа - 200")
                .isEqualTo(200);

        JsonPath json = response.jsonPath();

        assertThat(json.getInt("id"))
                .as("ID товара в теле ответа соответствует искомому")
                .isEqualTo(expectedId);

        assertThat(json.getString("name"))
                .as("Название товара " + expectedName)
                .isEqualTo(expectedName);

        assertThat(json.getDouble("price"))
                .as("Цена товара " + expectedPrice)
                .isEqualTo(expectedPrice);
    }

    @Tag("api")
    @Test
    @DisplayName("Товар не найден по ID")
    void productNotFoundById() {

        int expectedId = 3;

        Response response = given()
                .spec(requestSpec)
                .pathParam("id", expectedId)
                .get("/goods/{id}")
                .then()
                .spec(RestApiAssert.notFoundResponseSpec)
                .extract().response();

        assertThat(response.statusCode())
                .as("Код ответа - 404")
                .isEqualTo(404);

        JsonPath jsonPath = response.jsonPath();

        assertThat(jsonPath.getString("message"))
                .as("Появляется сообщение об ошибке")
                .isEqualTo("Good with id '%d' is not found!".formatted(expectedId));

    }

    @Tag("api")
    @Test
    @DisplayName("Редактирование цены товара")
    void updateProduct() {

        int expectedId = 1;
        String expectedName = "Кувшин";
        double expectedPrice = 60.0;

        Response response = given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body("""
                        {
                           "name": "Кувшин",
                           "price": 60
                        }
                        """)
                .pathParam("id", expectedId)
                .patch("/goods/{id}")
                .then()
                .spec(RestApiAssert.successResponseSpec)
                .extract().response();

        assertThat(response.statusCode())
                .as("Код ответа - 200")
                .isEqualTo(200);

        JsonPath json = response.jsonPath();

        assertThat(json.getInt("id"))
                .as("ID товара в теле ответа соответствует обновляемому")
                .isEqualTo(expectedId);

        assertThat(json.getString("name"))
                .as("Название товара - " + expectedName)
                .isEqualTo(expectedName);

        assertThat(json.getDouble("price"))
                .as("Цена товара изменилась на " + expectedPrice)
                .isEqualTo(expectedPrice);

    }

    @Tag("api")
    @Test
    @DisplayName("Попытка установить отрицательную цену товару")
    void updateProduct400() {
        Response response = given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body("""
                        {
                           "name": "Кувшин",
                           "price": -20
                        }
                        """)
                .pathParam("id", 1)
                .patch("/goods/{id}")
                .then()
                .spec(RestApiAssert.errorResponseSpec)
                .extract().response();

        assertThat(response.statusCode())
                .as("Код ответа - 400")
                .isEqualTo(400);

        JsonPath jsonPath = response.jsonPath();

        assertThat(jsonPath.getString("price"))
                .as("Появляется сообщение об ошибке")
                .isEqualTo("Price can not be less, than 0");

    }

    @Tag("api")
    @Test
    @DisplayName("Товар не найден при редактировании")
    void productNotFoundDuringUpdate() {

        int expectedId = 3;

        Response response = given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body("""
                        {
                           "name": "Кувшин",
                           "price": 20
                        }
                        """)
                .pathParam("id", expectedId)
                .patch("/goods/{id}")
                .then()
                .spec(RestApiAssert.notFoundResponseSpec)
                .extract().response();

        assertThat(response.statusCode())
                .as("Код ответа - 404")
                .isEqualTo(404);

        String errorText = response.asString();

        assertThat(errorText)
                .as("Текст ответа содержит сообщение, что тавар не найден")
                .contains("Good with id '%d' is not found!".formatted(expectedId));

    }

    @Tag("api")
    @Test
    @DisplayName("Успешное удаление товара")
    void removeItem() {

        int expectedId = 3;

        Response response = given()
                .spec(requestSpec)
                .pathParam("id", expectedId)
                .delete("/goods/{id}")
                .then()
                .spec(RestApiAssert.successResponseSpec)
                .extract().response();

        assertThat(response.statusCode())
                .as("Код ответа - 200")
                .isEqualTo(200);

        String successText = response.asString();

        assertThat(successText)
                .as("Текст ответа содержит сообщение об успешном удалении товара")
                .contains("Good with id '%d' has been deleted successfully!".formatted(expectedId));

    }

    @Tag("api")
    @Test
    @DisplayName("Попытка удаления несуществующего товара")
    void removeItem404() {

        int expectedId = 1;

        Response response = given()
                .spec(requestSpec)
                .pathParam("id", expectedId)
                .delete("/goods/{id}")
                .then()
                .spec(RestApiAssert.notFoundResponseSpec)
                .extract().response();

        assertThat(response.statusCode())
                .as("Код ответа - 404")
                .isEqualTo(404);

        String errorText = response.asString();

        assertThat(errorText)
                .as("Текст ответа содержит сообщение об отсутствии товара")
                .contains("Good with id '%d' is not found".formatted(expectedId));

    }

}
