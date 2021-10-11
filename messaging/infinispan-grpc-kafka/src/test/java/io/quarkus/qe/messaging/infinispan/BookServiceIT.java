package io.quarkus.qe.messaging.infinispan;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpStatus;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.Test;

import io.quarkus.qe.messaging.infinispan.books.Book;
import io.quarkus.test.bootstrap.InfinispanService;
import io.quarkus.test.bootstrap.KafkaService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.KafkaContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.containers.model.KafkaProtocol;
import io.quarkus.test.services.containers.model.KafkaVendor;
import io.restassured.http.ContentType;

@QuarkusScenario
public class BookServiceIT {

    /**
     * We can't rename this file to use the default SSL settings part of KafkaService.
     */
    private static final String TRUSTSTORE_FILE = "strimzi-server-ssl-truststore.p12";
    private static final String BOOK_TITLE = "testBook";
    private static final Book BOOK = new Book(BOOK_TITLE, "description", 2011);

    @Container(image = "${infinispan.image}", expectedLog = "${infinispan.expected-log}", port = 11222)
    static final InfinispanService infinispan = new InfinispanService()
            .withConfigFile("infinispan-config.yaml")
            .withSecretFiles("server.jks");

    @KafkaContainer(vendor = KafkaVendor.STRIMZI, protocol = KafkaProtocol.SSL, kafkaConfigResources = TRUSTSTORE_FILE)
    static final KafkaService kafkassl = new KafkaService();

    @KafkaContainer(vendor = KafkaVendor.STRIMZI, protocol = KafkaProtocol.SASL)
    static final KafkaService kafkasasl = new KafkaService();

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("quarkus.infinispan-client.server-list", infinispan::getInfinispanServerAddress)
            .withProperty("quarkus.infinispan-client.auth-username", infinispan.getUsername())
            .withProperty("quarkus.infinispan-client.auth-password", infinispan.getPassword())
            .withProperty("quarkus.infinispan-client.trust-store", "secret::/server.jks")
            .withProperty("quarkus.infinispan-client.trust-store-password", "changeit")
            .withProperty("quarkus.infinispan-client.trust-store-type", "jks")
            .withProperty("kafka.bootstrap.servers", kafkassl::getBootstrapUrl)
            .withProperty("kafka.ssl.enable", "true")
            .withProperty("kafka.ssl.truststore.location", TRUSTSTORE_FILE)
            .withProperty("kafka.ssl.truststore.password", "top-secret")
            .withProperty("kafka.ssl.truststore.type", "PKCS12")
            .withProperty("kafka-streams.state.dir", "target")
            .withProperty("quarkus.kafka-streams.ssl.endpoint-identification-algorithm", "")
            .withProperty("quarkus.kafka-streams.bootstrap-servers", kafkassl::getBootstrapUrl)
            .withProperty("kafka-client-ssl.bootstrap.servers", kafkassl::getBootstrapUrl)
            .withProperty("kafka-client-sasl.bootstrap.servers", kafkasasl::getBootstrapUrl);

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
    void testKafkaClientSASL() {
        await().untilAsserted(() -> {
            given()
                    .queryParam("key", "my-key")
                    .queryParam("value", "my-value")
                    .when()
                    .post("/kafka/sasl")
                    .then()
                    .statusCode(200);

            get("/kafka/sasl")
                    .then()
                    .statusCode(200)
                    .body(StringContains.containsString("my-key-my-value"));
        });

        get("/kafka/sasl/topics")
                .then()
                .statusCode(200)
                .body(StringContains.containsString("hello"));
    }

    @Test
    void testKafkaClientSSL() {
        await().untilAsserted(() -> {
            given()
                    .queryParam("key", "my-key")
                    .queryParam("value", "my-value")
                    .when()
                    .post("/kafka/ssl")
                    .then()
                    .statusCode(200);

            get("/kafka/ssl")
                    .then()
                    .statusCode(200)
                    .body(StringContains.containsString("my-key-my-value"));
        });

        get("/kafka/ssl/topics")
                .then()
                .statusCode(200)
                .body(StringContains.containsString("hello"));
    }
}
