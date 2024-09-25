package io.quarkus.ts.spring.cloud.config;

import static org.hamcrest.Matchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/2025")
public class SpringCloudConfigIT {

    @Container(image = "${spring.cloud.server.image}", port = 8888, expectedLog = "Started ConfigServer")
    static RestService spring = new RestService()
            .withProperty("SPRING_PROPERTY_FILE", "resource::/config/application-SpringCloudConfigIT.properties")
            .withProperty("SPRING_PROFILES_ACTIVE", "native");

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.profile", "SpringCloudConfigIT")
            .withProperty("quarkus.spring-cloud-config.url", () -> spring.getURI(Protocol.HTTP).toString());

    @Tag("QUARKUS-1218")
    @ParameterizedTest
    @ValueSource(strings = { "/jakarta-rest", "/spring", "/custom-mapping" })
    public void shouldGetExpectedHelloMessage(String rootPath) {
        app.given().get(rootPath + "/hello").then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("Hello from Spring Cloud Server"));
    }
}
