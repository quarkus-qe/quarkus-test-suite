package io.quarkus.ts.funqy.knativeevents;

import static io.quarkus.ts.funqy.knativeevents.Constants.CUSTOM_EVENT_ATTR_NAME;
import static io.quarkus.ts.funqy.knativeevents.Constants.CUSTOM_EVENT_ATTR_VALUE;
import static io.quarkus.ts.funqy.knativeevents.Constants.ENV_VAR_NAME;
import static io.quarkus.ts.funqy.knativeevents.Constants.ENV_VAR_VALUE;
import static io.quarkus.ts.funqy.knativeevents.Constants.PING_RESPONSE_SOURCE;
import static io.quarkus.ts.funqy.knativeevents.Constants.PING_RESPONSE_TYPE;
import static io.quarkus.ts.funqy.knativeevents.Constants.PING_TRIGGER;
import static io.quarkus.ts.funqy.knativeevents.Constants.ULTIMATE_QUESTION;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.RestAssured;

@QuarkusScenario
public class FunqyKnEventsIT {

    @QuarkusApplication
    static RestService service = new RestService().withProperty(ENV_VAR_NAME, ENV_VAR_VALUE);

    @Test
    public void testHttpRequest() {
        RestAssured.given().contentType("application/json")
                .body("\"messi\"")
                .post("/toUpperCase")
                .then()
                .statusCode(200)
                .body(Matchers.containsString("MESSI"));
    }

    @Test
    public void testCloudEventBinaryMode() {
        RestAssured.given()
                .contentType("application/json")
                .header("ce-id", "test-id")
                .header("ce-type", PING_TRIGGER)
                .header("ce-source", "test-src")
                .header("ce-subject", "test-subj")
                .header("ce-time", "2018-04-05T17:31:00Z")
                .header("ce-" + CUSTOM_EVENT_ATTR_NAME, CUSTOM_EVENT_ATTR_VALUE)
                .header("ce-specversion", "1.0")
                .header("ce-dataschema", "test-dataschema-cl")
                .body("true")
                .post("/")
                .then()
                .statusCode(200)
                .header("ce-specversion", equalTo("1.0"))
                .header("ce-type", equalTo(PING_RESPONSE_TYPE))
                .header("ce-source", equalTo(PING_RESPONSE_SOURCE))
                .body(notNullValue());
    }

    @Test
    public void testStructuredMode() {
        final String event = "{ \"id\" : \"test-id\", " +
                "  \"specversion\": \"1.0\", " +
                "  \"source\": \"test\", " +
                "  \"subject\": \"test-subj\", " +
                "  \"time\": \"2018-04-05T17:31:00Z\", " +
                "  \"type\": \"pung\", " +
                "  \"datacontenttype\": \"application/json\", " +
                "  \"data\": { \"question\" : \"" + ULTIMATE_QUESTION + "\", \"answer\" : 42 } " +
                "}";
        RestAssured.given()
                .contentType("application/cloudevents+json")
                .body(event)
                .post("/")
                .then()
                .statusCode(200)
                .body("specversion", equalTo("1.0"))
                .body("id", equalTo("one-two-three"))
                .body("type", equalTo("peng"))
                .body("source", equalTo("pung"))
                .body("data.id", equalTo("type"));
    }

}
