package io.quarkus.ts.messaging.infinispan.grpc.kafka;

import static io.quarkus.test.services.Certificate.Format.PKCS12;
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

import io.quarkus.test.bootstrap.InfinispanService;
import io.quarkus.test.bootstrap.KafkaService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.KafkaContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.containers.model.KafkaVendor;
import io.quarkus.ts.messaging.infinispan.grpc.kafka.books.Book;
import io.restassured.http.ContentType;

@Tag("QUARKUS-2036")
@QuarkusScenario
public class InfinispanKafkaIT {

    private static final String BOOK_TITLE = "testBook";
    private static final Book BOOK = new Book(BOOK_TITLE, "description", 2011);

    @Container(image = "${infinispan.image}", expectedLog = "${infinispan.expected-log}", port = 11222, command = "-c /infinispan-config.xml")
    static final InfinispanService infinispan = new InfinispanService()
            .withConfigFile("infinispan-config.xml")
            .withSecretFiles(CertUtils.KEYSTORE)
            .onPreStart((action) -> CertUtils.prepareCerts());

    @KafkaContainer(vendor = KafkaVendor.CONFLUENT)
    static final KafkaService kafka = new KafkaService();

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("quarkus.infinispan-client.hosts", infinispan::getInfinispanServerAddress)
            .withProperty("quarkus.infinispan-client.username", infinispan.getUsername())
            .withProperty("quarkus.infinispan-client.password", infinispan.getPassword())
            .withProperty("quarkus.infinispan-client.trust-store", CertUtils.getTruststorePath())
            .withProperty("quarkus.infinispan-client.trust-store-password", CertUtils.PASSWORD)
            .withProperty("quarkus.infinispan-client.trust-store-type", PKCS12.toString())
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
