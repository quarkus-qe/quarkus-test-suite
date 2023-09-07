package io.quarkus.ts.elasticsearch;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.DefaultService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;

@QuarkusScenario
public class ElasticsearchIT {

    static final int ELASTIC_PORT = 9200;

    @Container(image = "${elastic.7x.image}", port = ELASTIC_PORT, expectedLog = "started")
    static DefaultService elastic = new DefaultService()
            .withProperty("discovery.type", "single-node")
            .withProperty("ES_JAVA_OPTS", "-Xms512m -Xmx512m")
            .withProperty("cluster.routing.allocation.disk.threshold_enabled", "false")
            .withProperty("xpack.security.enabled", "false");

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.elasticsearch.hosts", () -> elastic.getURI(Protocol.NONE).toString());

    protected RestService getApp() {
        return app;
    }

    @Test
    void dataCreatedAndAccessible() {
        // CREATE content
        given()
                .contentType(ContentType.JSON)
                .body("{\"name\": \"apples\", \"color\": \"green\"}")
                .when()
                .post("fruits")
                .then()
                .statusCode(HttpStatus.SC_CREATED);

        // SEARCH content
        final StringBuilder sb = new StringBuilder();
        // Elasticsearch is creating index and mapping, thus await loop
        await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            String id = given()
                    .when()
                    .get("fruits/search?color=green")
                    .then()
                    .statusCode(HttpStatus.SC_OK)
                    .body(containsString("apples"))
                    .extract().path("[0].id");
            sb.append(id);
        });

        // DELETE content
        given()
                .when()
                .delete("fruits/" + sb)
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
        // wait for Elasticsearch to finish the work
        await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            given()
                    .when()
                    .get("fruits/search?color=green")
                    .then()
                    .statusCode(HttpStatus.SC_OK)
                    .body("isEmpty()", is(true));
        });
    }

    @Test
    void dataTypesCheck() {
        DataTypes fooDataTypes = new DataTypes();
        fooDataTypes.id = UUID.randomUUID().toString();
        fooDataTypes.name = "Foo name ěščřžýáíéůú ťďň";
        fooDataTypes.fruits = List.of(new Fruit("Fruit #1", "Red"), new Fruit("Fruit #2", "Yellow"));
        fooDataTypes.date = new Date();
        fooDataTypes.floatNum = 0.2353f;
        fooDataTypes.doubleNum = 15.698d;

        Response response = given()
                .contentType(ContentType.JSON)
                .body(JsonObject.mapFrom(fooDataTypes).toString())
                .when()
                .post("data-types");

        response.then()
                .statusCode(HttpStatus.SC_OK);

        DataTypes returnedFooDataTypes = response.getBody().as(DataTypes.class);
        Assert.assertEquals(fooDataTypes, returnedFooDataTypes);
    }

    @Test
    void healthCheckAvailable() {
        given()
                .when()
                .get("q/health")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(containsString("Elasticsearch cluster health check"));
    }
}
