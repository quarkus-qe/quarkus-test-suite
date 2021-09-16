package io.quarkus.ts.nosqldb.mongodb;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.quarkus.test.bootstrap.MongoDbService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpRequest;
import io.vertx.mutiny.ext.web.client.HttpResponse;

@QuarkusScenario
public class MongoDbIT {

    @Container(image = "${mongodb.image}", port = 27017, expectedLog = "Waiting for connections")
    static MongoDbService database = new MongoDbService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.mongodb.connection-string", database::getJdbcUrl);

    @ParameterizedTest
    @ValueSource(strings = { "/reactive_fruits" })
    public void fruitsEndpoints(String path) {
        final Fruit fruit1 = new Fruit("fruit1", "fruit description 1");
        final Fruit fruit2 = new Fruit("fruit2", "fruit description 2");

        List<Fruit> fruits1 = postEntity(path, fruit1, Fruit.class);
        Assertions.assertThat(fruits1).isNotNull();
        Assertions.assertThat(fruits1.size()).isEqualTo(1);
        Assertions.assertThat(fruits1).contains(fruit1);

        fruits1 = postEntity(path, fruit2, Fruit.class);
        Assertions.assertThat(fruits1.size()).isEqualTo(2);
        Assertions.assertThat(fruits1).contains(fruit1, fruit2);

        List<Fruit> fruits2 = getEntities(path, Fruit.class);
        Assertions.assertThat(fruits2).isEqualTo(fruits1);
    }

    @ParameterizedTest
    @ValueSource(strings = { "/reactive_fruit_baskets" })
    public void fruitBasketsEndpoints(String path) {
        final Fruit fruit1 = new Fruit("fruit1", "fruit description 1");
        final Fruit fruit2 = new Fruit("fruit2", "fruit description 2");
        final FruitBasket fruitBasket1 = new FruitBasket("null", null);
        final FruitBasket fruitBasket2 = new FruitBasket("empty", List.of());
        final FruitBasket fruitBasket3 = new FruitBasket("full", List.of(fruit1, fruit2));

        List<FruitBasket> fruitBaskets1 = postEntity(path, fruitBasket1, FruitBasket.class);
        Assertions.assertThat(fruitBaskets1).isNotNull();
        Assertions.assertThat(fruitBaskets1.size()).isEqualTo(1);
        Assertions.assertThat(fruitBaskets1).contains(fruitBasket1);
        Assertions.assertThat(fruitBaskets1.get(0).getItems()).isNull();

        fruitBaskets1 = postEntity(path, fruitBasket2, FruitBasket.class);
        Assertions.assertThat(fruitBaskets1).isNotNull();
        Assertions.assertThat(fruitBaskets1.size()).isEqualTo(2);
        Assertions.assertThat(fruitBaskets1).contains(fruitBasket1, fruitBasket2);
        Assertions.assertThat(fruitBaskets1.get(1).getItems()).isEmpty();

        fruitBaskets1 = postEntity(path, fruitBasket3, FruitBasket.class);
        Assertions.assertThat(fruitBaskets1).isNotNull();
        Assertions.assertThat(fruitBaskets1.size()).isEqualTo(3);
        Assertions.assertThat(fruitBaskets1).contains(fruitBasket1, fruitBasket2, fruitBasket3);
        Assertions.assertThat(fruitBaskets1.get(2).getItems()).contains(fruit1, fruit2);

        List<FruitBasket> fruitBaskets2 = getEntities(path, FruitBasket.class);
        Assertions.assertThat(fruitBaskets2).isEqualTo(fruitBaskets1);

        List<FruitBasket> fruitBaskets3 = getEntities(path + "/find-items/" + fruitBasket3.getName(), FruitBasket.class);
        Assertions.assertThat(fruitBaskets3).isNotNull();
        Assertions.assertThat(fruitBaskets3.size()).isEqualTo(1);
        Assertions.assertThat(fruitBaskets3.get(0).getName()).isNull();
        Assertions.assertThat(fruitBaskets3.get(0).getId()).isNull();
        Assertions.assertThat(fruitBaskets3.get(0).getItems()).isEqualTo(fruitBasket3.getItems());
    }

    private <T> List<T> postEntity(String path, T entity, Class<T> clazz) {
        final HttpRequest<Buffer> request = createRequest(path, HttpMethod.POST);
        final HttpResponse<Buffer> response = getResponse(request.sendJson(entity));
        Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
        return parseJsonArrayResponse(response, clazz);
    }

    private <T> List<T> getEntities(String path, Class<T> clazz) {
        final HttpRequest<Buffer> request = createRequest(path, HttpMethod.GET);
        final HttpResponse<Buffer> response = getResponse(request.send());
        Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
        return parseJsonArrayResponse(response, clazz);
    }

    private HttpRequest<Buffer> createRequest(String path, HttpMethod httpMethod) {
        return app.mutiny().request(httpMethod, path);
    }

    private HttpResponse<Buffer> getResponse(Uni<HttpResponse<Buffer>> responseUni) {
        return responseUni.subscribe().withSubscriber(UniAssertSubscriber.create()).awaitItem().assertCompleted().getItem();
    }

    private <T> List<T> parseJsonArrayResponse(HttpResponse<Buffer> response, Class<T> clazz) {
        return response.bodyAsJsonArray().stream().map(value -> Json.decodeValue(value.toString(), clazz))
                .collect(Collectors.toList());
    }
}
