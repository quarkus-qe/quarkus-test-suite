package io.quarkus.ts.spring.web.reactive.reactive;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import io.quarkus.test.bootstrap.RestService;
import io.restassured.http.ContentType;

public abstract class AbstractDbReactiveIT {

    private static final String APP_NAME = "Bootstrap Spring Boot";

    @Test
    public void shouldQuteReplaceWelcomePhrase() {
        getApp().given().get("/")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.HTML)
                .body(CoreMatchers.containsString(APP_NAME));
    }

    public abstract RestService getApp();
}
