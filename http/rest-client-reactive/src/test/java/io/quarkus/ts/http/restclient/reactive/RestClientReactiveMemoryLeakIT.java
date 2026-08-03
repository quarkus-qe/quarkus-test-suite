package io.quarkus.ts.http.restclient.reactive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.logging.Log;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

import oom.EventsClient;
import oom.EventsClientResource;
import oom.EventsResource;

@QuarkusScenario
public class RestClientReactiveMemoryLeakIT {

    @QuarkusApplication(classes = EventsResource.class)
    static RestService server = new RestService();

    // If the app was created with framework, then the only way to pass java args to it is to use -Djvm.args and dev mode
    @DevModeQuarkusApplication(classes = { EventsClient.class, EventsClientResource.class }, properties = "modern.properties")
    static RestService client = new RestService()
            .withProperty("quarkus.rest-client.logging.scope", "none")
            .withProperty("jvm.args", "-Xmx100m -XX:+HeapDumpOnOutOfMemoryError -XX:+ExitOnOutOfMemoryError")
            .withProperty("quarkus.rest-client.events-client.url", () -> server.getURI(Protocol.HTTP).toString());

    @Test
    @Tag("QUARKUS-8429")
    @Tag("QUARKUS-8425")
    @Tag("https://github.com/quarkusio/quarkus/pull/54659")
    public void httpServer() {
        Response response = client.given().get("/client/events");
        assertEquals(200, response.statusCode());
        long count = Long.parseLong(response.getBody().asString());
        assertNotEquals(0, count);
        try {
            while (count < (200_000)) { // as of 3.36.1, the job fails around 100 000, but let's have some margin for error
                response = client.given().get("/client/events");
                assertEquals(200, response.statusCode());
                count = Long.parseLong(response.getBody().asString());
                if (count % 1000 == 0) {
                    Log.info("Current count is " + count);
                }
            }
        } catch (Exception ex) {
            Assertions.fail("Reading from the endpoint failed  after " + count + " requests was it due to OOM?", ex);
        }
    }
}
