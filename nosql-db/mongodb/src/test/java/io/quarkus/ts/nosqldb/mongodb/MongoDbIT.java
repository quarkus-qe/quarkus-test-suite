package io.quarkus.ts.nosqldb.mongodb;

import org.apache.http.HttpStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.quarkus.test.bootstrap.MongoDbService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@QuarkusScenario
public class MongoDbIT {

    @Container(image = "${mongodb.image}", port = 27017, expectedLog = "Waiting for connections")
    static MongoDbService database = new MongoDbService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.mongodb.connection-string", database::getJdbcUrl);

    @ParameterizedTest
    @ValueSource(strings = { "/fruits", "/reactive_fruits", "/codec_fruits" })
    public void testAddAndList(String path) {
        final Fruit fruit1 = new Fruit("fruit1", "fruit description 1");
        final Fruit fruit2 = new Fruit("fruit2", "fruit description 2");

        Fruit[] fruits1 = postFruit(path, fruit1);
        Assertions.assertThat(fruits1).isNotNull();
        Assertions.assertThat(fruits1.length).isEqualTo(1);
        Assertions.assertThat(fruits1[0]).isEqualTo(fruit1);

        fruits1 = postFruit(path, fruit2);
        Assertions.assertThat(fruits1.length).isEqualTo(2);
        Assertions.assertThat(fruits1).contains(fruit1, fruit2);

        Fruit[] fruits2 = RestAssured.get(path)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().body().as(Fruit[].class);
        Assertions.assertThat(fruits2).isEqualTo(fruits1);
    }

    private Fruit[] postFruit(String path, Fruit fruit) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(fruit)
                .post(path)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().body().as(Fruit[].class);
    }
}
