package io.quarkus.ts.properties.bulk;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

import org.apache.http.HttpStatus;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;

@QuarkusScenario
public class ConfigValueIT {

    @Test
    public void shouldInjectConfigValueServerHost() {
        assertResponseIs("/serverUrl/name", "server.url");
        assertResponseIs("/serverUrl/value", "http://example.org/endpoint");
        assertResponseContains("/serverUrl/sourceName", getExpectedSourceNameValue());
        assertResponseIs("/serverUrl/rawValue", "http://${server.host}/endpoint");
    }

    private <T> void assertResponseIs(String path, T expected) {
        assertResponse(path, is(expected.toString()));
    }

    private <T> void assertResponseContains(String path, T expected) {
        assertResponse(path, containsString(expected.toString()));
    }

    private void assertResponse(String path, Matcher<String> matcher) {
        given().when().get("/config-value" + path)
                .then().statusCode(HttpStatus.SC_OK)
                .body(matcher);
    }

    // Clarification: https://github.com/quarkusio/quarkus/issues/27231#issuecomment-1211783882
    private String getExpectedSourceNameValue() {
        return isNativeExecution() ? "RunTime Defaults" : "PropertiesConfigSource";
    }

    private boolean isNativeExecution() {
        return System.getProperties().contains("native");
    }
}
