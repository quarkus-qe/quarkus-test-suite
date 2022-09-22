package io.quarkus.ts.qute;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

@QuarkusScenario
public class MessageBundlesIT {
    @QuarkusApplication
    static RestService app = new RestService();

    @Test
    void smoke() {
        Response response = app.given().get("message/en");
        assertEquals(200, response.statusCode());
        assertEquals("Hello, Dr. Livingstone!", response.body().asString());
    }

    @Test
    void fromClass() {
        Response response = app.given().get("message/es");
        assertEquals(200, response.statusCode());
        assertEquals("Hola, Dr. Livingstone!", response.body().asString());
    }

    @Test
    void fromFile() {
        Response response = app.given().get("message/cs");
        assertEquals(200, response.statusCode());
        assertEquals("Ahoj, Dr. Livingstone!", response.body().asString());
    }

    @Test
    void rtl() {
        Response response = app.given().get("message/he");
        assertEquals(200, response.statusCode());
        assertEquals("שלם, Dr. Livingstone!", response.body().asString());
    }

    @Test
    void multiline() {
        Response response = app.given().get("message/long/en");
        assertEquals(200, response.statusCode());
        assertEquals("Hello, Dr. Livingstone! \n How are you, Dr. Livingstone?", response.body().asString());
    }

    @Test
    void defaultMessage() {
        Response response = app.given().get("message/long/es");
        assertEquals(200, response.statusCode());
        assertEquals("Hello, Dr. Livingstone! \n How are you, Dr. Livingstone?", response.body().asString());
    }

    @Test
    void multilineRTL() {
        Response response = app.given().get("message/long/he");
        assertEquals(200, response.statusCode());
        String content = response.body().asString();
        assertEquals("שלם, Dr. Livingstone, מה נשמע?", content);
    }

    @Test
    void injected() {
        Response response = app.given().get("message/");
        assertEquals(200, response.statusCode());
        String content = response.body().asString();
        assertEquals("שלם, אדם!", content);
    }

}
