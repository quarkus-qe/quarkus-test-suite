package io.quarkus.ts.http.advanced.reactive;

import static io.quarkus.ts.http.advanced.reactive.Brotli4JHttpIT.CONTENT_LENGTH_DEFAULT_TEXT_PLAIN;
import static io.quarkus.ts.http.advanced.reactive.brotli4j.Brotli4JResource.DEFAULT_TEXT_PLAIN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.DevModeQuarkusService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.ts.http.advanced.reactive.brotli4j.Brotli4JHttpServerConfig;
import io.quarkus.ts.http.advanced.reactive.brotli4j.Brotli4JResource;
import io.quarkus.ts.http.advanced.reactive.brotli4j.Brotli4JRestMock;
import io.restassured.response.Response;

@QuarkusScenario
public class DevModeBrotli4JHttpIT {
    private static final String COMPRESSION_ENABLED_PROPERTY = "quarkus.http.enable-compression";
    @DevModeQuarkusApplication(classes = { Brotli4JHttpServerConfig.class, Brotli4JResource.class,
            Brotli4JRestMock.class }, properties = "compression.properties")
    static DevModeQuarkusService app = (DevModeQuarkusService) new DevModeQuarkusService()
            .withProperty(COMPRESSION_ENABLED_PROPERTY, "false");

    @Test
    public void disableCompression() {
        Response response = app.given()
                .when()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.ACCEPT_ENCODING, "br")
                .get("/compression/default/text")
                .then()
                .statusCode(200)
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(CONTENT_LENGTH_DEFAULT_TEXT_PLAIN))
                .header(HttpHeaders.CONTENT_ENCODING, nullValue())
                .body(is(DEFAULT_TEXT_PLAIN))
                .extract().response();
        assertThat("Body length should not be less than CONTENT_LENGTH_DEFAULT_TEXT_PLAIN",
                response.body().asString().length(), equalTo(CONTENT_LENGTH_DEFAULT_TEXT_PLAIN));
    }
}
