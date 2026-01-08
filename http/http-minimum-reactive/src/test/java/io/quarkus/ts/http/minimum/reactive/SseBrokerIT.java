package io.quarkus.ts.http.minimum.reactive;

import static org.apache.http.HttpStatus.SC_NO_CONTENT;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.sse.SseEventSource;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class SseBrokerIT {

    // issue will not manifest if Jakarta Interceptor is present in the app
    @QuarkusApplication(classes = SseBrokerResource.class)
    static RestService app = new RestService();

    @Test
    @Tag("https://github.com/quarkusio/quarkus/issues/51035")
    public void testSseBrokerNativeIssue() {
        // there is an issue with quarkus and SseEvenSink, when it fails with NoSuchMethodException on native

        // open SSE consumer. We need it to be open during SSE send, for issue to manifest
        WebTarget target = ClientBuilder.newClient().target(app.getURI(Protocol.HTTP) + "/api/sse-broker/read");
        SseEventSource updateSource = SseEventSource.target(target).build();
        updateSource.register(ev -> {
            // do nothing with the event, we just have to be subscribed
        });
        updateSource.open();

        // send SSE event
        // if no issue manifests itself, there should be no problem, and server should return 204 - no content
        // if there is an issue, server will return 500
        app.given()
                .param("hello")
                .put("/api/sse-broker/send")
                .then().statusCode(SC_NO_CONTENT);
    }
}
