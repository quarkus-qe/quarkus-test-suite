package io.quarkus.ts.nosqldb.mongodb;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.apache.http.HttpStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.quarkus.test.bootstrap.MongoDbService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;

@QuarkusScenario
public class MongoDbIT {

    @Container(image = "${mongodb.image}", port = 27017, expectedLog = "Waiting for connections")
    static MongoDbService database = new MongoDbService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.mongodb.connection-string", database::getJdbcUrl);

    @ParameterizedTest
    @ValueSource(strings = { "/fruits", "/codec_fruits" })
    public void fruitsEndpoints(String path) {
        final Fruit fruit1 = new Fruit("fruit1", "fruit description 1");
        final Fruit fruit2 = new Fruit("fruit2", "fruit description 2");

        List<Fruit> fruits1 = postFruit(path, fruit1);
        assertThat(fruits1).isNotNull();
        assertThat(fruits1.size()).isEqualTo(1);
        assertThat(fruits1).contains(fruit1);

        fruits1 = postFruit(path, fruit2);
        assertThat(fruits1.size()).isEqualTo(2);
        assertThat(fruits1).contains(fruit1, fruit2);

        List<Fruit> fruits2 = RestAssured.get(path)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().body().jsonPath().getList(".", Fruit.class);
        assertThat(fruits2).isEqualTo(fruits1);
    }

    @ParameterizedTest
    @ValueSource(strings = { "/fruit_baskets", "/codec_fruit_baskets" })
    public void fruitBasketsEndpoints(String path) {
        final Fruit fruit1 = new Fruit("fruit1", "fruit description 1");
        final Fruit fruit2 = new Fruit("fruit2", "fruit description 2");
        final FruitBasket fruitBasket1 = new FruitBasket("null", null);
        final FruitBasket fruitBasket2 = new FruitBasket("empty", List.of());
        final FruitBasket fruitBasket3 = new FruitBasket("full", List.of(fruit1, fruit2));

        List<FruitBasket> fruitBaskets1 = postFruitBasket(path, fruitBasket1);
        assertThat(fruitBaskets1).isNotNull();
        assertThat(fruitBaskets1.size()).isEqualTo(1);
        assertThat(fruitBaskets1).contains(fruitBasket1);
        assertThat(fruitBaskets1.get(0).getItems()).isNull();

        fruitBaskets1 = postFruitBasket(path, fruitBasket2);
        assertThat(fruitBaskets1).isNotNull();
        assertThat(fruitBaskets1.size()).isEqualTo(2);
        assertThat(fruitBaskets1).contains(fruitBasket1, fruitBasket2);
        assertThat(fruitBaskets1.get(1).getItems()).isEmpty();

        fruitBaskets1 = postFruitBasket(path, fruitBasket3);
        assertThat(fruitBaskets1).isNotNull();
        assertThat(fruitBaskets1.size()).isEqualTo(3);
        assertThat(fruitBaskets1).contains(fruitBasket1, fruitBasket2, fruitBasket3);
        assertThat(fruitBaskets1.get(2).getItems()).contains(fruit1, fruit2);

        List<FruitBasket> fruitBaskets2 = RestAssured.get(path)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().body().jsonPath().getList(".", FruitBasket.class);
        assertThat(fruitBaskets2).isEqualTo(fruitBaskets1);

        List<FruitBasket> fruitBaskets3 = RestAssured.get(path + "/find-items/" + fruitBasket3.getName())
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().body().jsonPath().getList(".", FruitBasket.class);
        assertThat(fruitBaskets3).isNotNull();
        assertThat(fruitBaskets3.size()).isEqualTo(1);
        assertThat(fruitBaskets3.get(0).getName()).isNull();
        assertThat(fruitBaskets3.get(0).getId()).isNull();
        assertThat(fruitBaskets3.get(0).getItems()).isEqualTo(fruitBasket3.getItems());
    }

    private List<Fruit> postFruit(String path, Fruit fruit) {
        return postEntity(path, fruit).getList(".", Fruit.class);
    }

    private List<FruitBasket> postFruitBasket(String path, FruitBasket fruitBasket) {
        return postEntity(path, fruitBasket).getList(".", FruitBasket.class);
    }

    private <T> JsonPath postEntity(String path, T entity) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(entity)
                .post(path)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().body().jsonPath();
    }
}
