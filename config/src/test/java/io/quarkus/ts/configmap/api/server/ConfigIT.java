package io.quarkus.ts.configmap.api.server;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

@QuarkusScenario
public class ConfigIT {

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("the.answer", "42")
            .withProperty("secret.password", "T0tallySafePa\\$\\$word")
            .withProperty("secret.ip", "127.0.0.1");

    @Test
    void properties() {
        // when we inject properties directly, they are not marked as secret
        Response answer = app.given().get("/properties/locked/the.answer");
        Assertions.assertEquals(200, answer.statusCode());
        Assertions.assertEquals("42", answer.body().asString());

        Response password = app.given().get("/properties/locked/secret.password");
        Assertions.assertEquals(200, password.statusCode());
        Assertions.assertEquals("T0tallySafePa$$word", password.body().asString());

        Response ip = app.given().get("/properties/locked/secret.ip");
        Assertions.assertEquals(200, ip.statusCode());
        Assertions.assertEquals("127.0.0.1", ip.body().asString());
    }

    @Test
    void injectedConfig() {
        // when we inject config, properties are marked as secret by interceptor
        Response answer = app.given().get("/injected/locked/the.answer");
        Assertions.assertEquals(200, answer.statusCode());
        Assertions.assertEquals("42", answer.body().asString());

        Response password = app.given().get("/injected/locked/secret.password");
        Assertions.assertEquals(500, password.statusCode());
        Assertions.assertEquals("SRCFG00024: Not allowed to access secret key secret.password", password.body().asString());

        Response unlocked = app.given().get("/injected/unlocked/secret.password");
        Assertions.assertEquals(200, unlocked.statusCode());
        Assertions.assertEquals("T0tallySafePa$$word", unlocked.body().asString());

        Response ip = app.given().get("/injected/locked/secret.ip");
        Assertions.assertEquals(200, ip.statusCode());
        Assertions.assertEquals("127.0.0.1", ip.body().asString());
    }

    @Test
    void builtConfig() {
        // when we build properties, we choose, which ones we mark as secret
        Response answer = app.given().get("/built/locked/the.answer");
        Assertions.assertEquals(200, answer.statusCode());
        Assertions.assertEquals("42", answer.body().asString());

        Response unlocked = app.given().get("/built/locked/secret.password");
        Assertions.assertEquals(200, unlocked.statusCode());
        Assertions.assertEquals("T0tallySafePa$$word", unlocked.body().asString());

        Response password = app.given().get("/built/locked/secret.ip");
        Assertions.assertEquals(500, password.statusCode());
        Assertions.assertEquals("SRCFG00024: Not allowed to access secret key secret.ip", password.body().asString());

        Response ip = app.given().get("/built/unlocked/secret.ip");
        Assertions.assertEquals(200, ip.statusCode());
        Assertions.assertEquals("127.0.0.1", ip.body().asString());
    }

    @Test
    void unlocked() {
        // verify, what doUnlock doesn't mess with public values
        Response properties = app.given().get("/properties/unlocked/the.answer");
        Assertions.assertEquals(200, properties.statusCode());
        Assertions.assertEquals("42", properties.body().asString());

        Response answer = app.given().get("/injected/unlocked/the.answer");
        Assertions.assertEquals(200, answer.statusCode());
        Assertions.assertEquals("42", answer.body().asString());

        Response built = app.given().get("/built/unlocked/the.answer");
        Assertions.assertEquals(200, built.statusCode());
        Assertions.assertEquals("42", built.body().asString());
    }
}
