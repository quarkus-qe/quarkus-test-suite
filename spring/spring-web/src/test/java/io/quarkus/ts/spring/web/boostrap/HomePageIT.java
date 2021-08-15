package io.quarkus.ts.spring.web.boostrap;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.ts.spring.web.AbstractDbIT;
import io.restassured.http.ContentType;

@QuarkusScenario
public class HomePageIT extends AbstractDbIT {
    private static final String APP_NAME = "Bootstrap Spring Boot";

    @Test
    public void shouldQuteReplaceWelcomePhrase() {
        app.given().get("/")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.HTML)
                .body(CoreMatchers.containsString(APP_NAME));
    }
}
