package io.quarkus.ts.nosqldb.mongodb;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.MongoDbService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.Mount;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.http.ContentType;

@Tag("QUARKUS-6233")
@QuarkusScenario
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MongoClientMtlsIncorrectSettingsIT {

    private static final String INCORRECT_CONFIGURATION_NAME = "incorrectConfigName";
    private static final String KEYSTORE = "-keystore.p12";
    private static final String TRUSTSTORE = "-truststore.p12";
    private static final String CLIENT_PREFIX = "client";
    private static final String SERVER_PREFIX = "server";

    @Container(image = "${mongodb.image}", port = 27017, expectedLog = "Waiting for connections", command = { "--config",
            "/etc/mongod.conf" }, mounts = { @Mount(from = "mongod.conf", to = "/etc/mongod.conf"),
                    @Mount(from = "mongo-certs/ca.pem", to = "/etc/ssl/ca.pem"),
                    @Mount(from = "mongo-certs/mongodb.pem", to = "/etc/ssl/mongodb.pem") }, portDockerHostToLocalhost = true)
    static MongoDbService database = new MongoDbService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperties("mtls.properties")
            .withProperty("quarkus.mongodb.connection-string", () -> database.getJdbcUrl())
            .withProperty("quarkus.tls.mongo.key-store.p12.path", CLIENT_PREFIX + KEYSTORE)
            .withProperty("quarkus.tls.mongo.trust-store.p12.path", CLIENT_PREFIX + TRUSTSTORE);

    @Test
    @Order(1)
    public void testConnectionWorkCorrectly() {
        final Fruit fruit1 = new Fruit("fruit1", "fruit description 1");

        List<Fruit> fruits = given()
                .contentType(ContentType.JSON)
                .body(fruit1)
                .post("/fruits")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().body().jsonPath()
                .getList(".", Fruit.class);

        assertThat(fruits).isNotNull();
        assertThat(fruits.size()).isEqualTo(1);
        assertThat(fruits).contains(fruit1);
    }

    @Test
    @Order(2)
    public void testIncorrectTruststore() {
        restartAppWithIncorrectTrustStore();
        sendPostRequest();
        // Quarkus was able to connect to MongoDB, but the Quarkus not trust that connection and throw `SignatureException`
        app.logs().assertContains("com.mongodb.MongoSocketWriteException: Exception sending message",
                "java.security.SignatureException: Signature does not match.");
    }

    @Test
    @Order(3)
    public void testIncorrectKeystore() {
        restartAppWithIncorrectKeyStore();
        sendPostRequest();
        // Connection was closed by MongoDB as the keystore cert is unknown to MongoDB
        app.logs().assertContains("com.mongodb.MongoSocketReadException: Prematurely reached end of stream");
    }

    @Test
    @Order(4)
    public void testIncorrectConfigurationName() {
        failStartAppWithIncorrectConfigurationName();
    }

    private void sendPostRequest() {
        final Fruit fruit1 = new Fruit("fruit1", "fruit description 1");

        given()
                .contentType(ContentType.JSON)
                .body(fruit1)
                .post("/fruits")
                .then()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    private static void restartAppWithIncorrectTrustStore() {
        app.stop();
        app.withProperty("quarkus.tls.mongo.key-store.p12.path", CLIENT_PREFIX + TRUSTSTORE)
                .withProperty("quarkus.tls.mongo.trust-store.p12.path", SERVER_PREFIX + TRUSTSTORE);
        app.start();
    }

    private static void restartAppWithIncorrectKeyStore() {
        app.stop();
        app.withProperty("quarkus.tls.mongo.key-store.p12.path", SERVER_PREFIX + TRUSTSTORE)
                .withProperty("quarkus.tls.mongo.trust-store.p12.path", CLIENT_PREFIX + TRUSTSTORE);
        app.start();
    }

    private static void failStartAppWithIncorrectConfigurationName() {
        app.stop();
        app.withProperty("quarkus.mongodb.tls-configuration-name", INCORRECT_CONFIGURATION_NAME)
                .withProperty("quarkus.tls.mongo.key-store.p12.path", CLIENT_PREFIX + KEYSTORE)
                .withProperty("quarkus.tls.mongo.trust-store.p12.path", CLIENT_PREFIX + TRUSTSTORE);
        assertThrows(AssertionError.class, () -> app.start(),
                "Should fail to start because property quarkus.mongodb.tls-configuration-name' is set to incorrect configuration name");
        app.logs().assertContains("Unable to find the TLS configuration for name " + INCORRECT_CONFIGURATION_NAME);
    }
}
