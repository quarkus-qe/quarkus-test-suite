package io.quarkus.ts.spring.properties;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class SpringPropertiesIT {

    @QuarkusApplication
    static RestService app = new RestService();

    @Test
    public void shouldInjectPublicFieldFromConfigurationPropertiesIntoController() {
        // "hello" is defined in application.properties
        assertEquals("hello", getGreetingsWithPath("/text"));
    }

    @Test
    public void shouldInjectDefaultPublicFieldFromConfigurationPropertiesIntoController() {
        // "hola" is the default value in GreetingProperties.java as there is no property defined yet
        assertEquals("Hola", getGreetingsWithPath("/textWithDefault"));

        setApplicationProperty("greeting.text-with-default", "Alo!");
        assertEquals("Alo!", getGreetingsWithPath("/textWithDefault"));
    }

    @Test
    public void shouldInjectPrivateFieldFromConfigurationPropertiesIntoController() {
        // "private hello!" is defined in application.properties
        assertEquals("private hello!", getGreetingsWithPath("/textPrivate"));
    }

    @Test
    public void shouldInjectOptionalPublicFieldFromConfigurationPropertiesIntoController() {
        // "empty" is returned when the property is not in application.properties
        assertEquals("empty!", getGreetingsWithPath("/textOptional"));

        setApplicationProperty("greeting.text-optional", "Hi!");
        assertEquals("Hi!", getGreetingsWithPath("/textOptional"));
    }

    @Test
    public void shouldInjectGroupPublicFieldFromConfigurationPropertiesIntoController() {
        // "Hola unknown!" is returned when the property person is not in application.properties
        assertEquals("Hola unknown!", getGreetingsWithPath("/message"));

        setApplicationProperty("greeting.message.person", "Sarah");
        assertEquals("Hola Sarah!", getGreetingsWithPath("/message"));
    }

    @Test
    public void shouldInjectListsOfStringFromConfigurationProperties() {
        // These values comes from the application.properties
        assertEquals("Value 1", get("/collections/list/strings"));

        setApplicationProperty("lists.strings[1]", "Value 2");
        assertEquals("Value 1, Value 2", get("/collections/list/strings"));
    }

    @Test
    public void shouldInjectFieldsUsingValueAnnotation() {
        assertEquals("hello", getValuesWithPath("/fieldUsingValue"));
    }

    @Test
    public void shouldInjectArrayFieldsUsingValueAnnotation() {
        assertEquals("A, B", getValuesWithPath("/fieldUsingArray"));
    }

    private void setApplicationProperty(String name, String value) {
        app.stop();
        app.withProperty(name, value);
        app.start();
    }

    private String getValuesWithPath(String path) {
        return get("/values" + path);
    }

    private String getGreetingsWithPath(String path) {
        return get("/greeting" + path);
    }

    private String get(String path) {
        return given().when().get(path)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().asString();
    }
}
