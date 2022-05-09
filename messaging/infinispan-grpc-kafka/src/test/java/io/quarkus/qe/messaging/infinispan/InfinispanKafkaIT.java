package io.quarkus.qe.messaging.infinispan;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.apache.http.HttpStatus;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.qe.messaging.infinispan.books.Book;
import io.quarkus.test.bootstrap.InfinispanService;
import io.quarkus.test.bootstrap.KafkaService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.KafkaContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.containers.model.KafkaVendor;
import io.restassured.http.ContentType;

@QuarkusScenario
// TODO https://github.com/quarkusio/quarkus/issues/25136
@Tag("fips-incompatible")
public class InfinispanKafkaIT {

    private static final String BOOK_TITLE = "testBook";
    private static final Book BOOK = new Book(BOOK_TITLE, "description", 2011);

    @Container(image = "${infinispan.image}", expectedLog = "${infinispan.expected-log}", port = 11222)
    static final InfinispanService infinispan = new InfinispanService()
            .withConfigFile("infinispan-config.yaml")
            .withSecretFiles("server.jks");

    @KafkaContainer(vendor = KafkaVendor.CONFLUENT)
    static final KafkaService kafka = new KafkaService();

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("quarkus.infinispan-client.server-list", infinispan::getInfinispanServerAddress)
            .withProperty("quarkus.infinispan-client.auth-username", infinispan.getUsername())
            .withProperty("quarkus.infinispan-client.auth-password", infinispan.getPassword())
            .withProperty("quarkus.infinispan-client.trust-store", "secret::/server.jks")
            .withProperty("quarkus.infinispan-client.trust-store-password", "changeit")
            .withProperty("quarkus.infinispan-client.trust-store-type", "jks")
            .withProperty("kafka.bootstrap.servers", kafka::getBootstrapUrl);

    @Test
    public void testBookResource() {
        given()
                .contentType(ContentType.JSON)
                .body(BOOK)
                .when().post("/book/add")
                .then().statusCode(HttpStatus.SC_NO_CONTENT);

        Book actual = given()
                .accept(ContentType.JSON)
                .when().get("/book/" + BOOK_TITLE)
                .as(Book.class);

        assertEquals(BOOK, actual);
    }

    @Test
    public void testBookResourceShouldValidateBook() {
        given()
                .contentType(ContentType.JSON)
                .body(new Book())
                .when().post("/book/add")
                .then().statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(containsString("Title cannot be blank"));
    }

    @Test
    public void testBlockingGreetingResource() {
        given()
                .when().get("/hello/blocking/neo")
                .then().statusCode(200)
                .body(is("Hello neo"));
    }

    @Test
    public void testMutinyGreetingResource() {
        given()
                .when().get("/hello/mutiny/neo")
                .then().statusCode(200)
                .body(is("Hello neo"));
    }

    @Test
    public void testInfinispanEndpoint() {
        given()
                .when().get("/infinispan")
                .then()
                .statusCode(200)
                .body(is("Hello World, Infinispan is up!"));
    }

    @Test
    public void testPricesResource() {
        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            get("/prices/poll")
                    .then()
                    .statusCode(HttpStatus.SC_OK);
        });
    }
}
