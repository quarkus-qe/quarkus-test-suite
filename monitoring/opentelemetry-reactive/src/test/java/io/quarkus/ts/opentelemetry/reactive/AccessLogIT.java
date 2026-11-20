package io.quarkus.ts.opentelemetry.reactive;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

// TODO Quarkus 4: OTel scope closes MDC (traceId/spanId) before the access log handler reads it
// on reactive endpoints running on the event loop. The span ends at headers time
// (observeOutboundHeaders) but the access log reads MDC in the response endHandler.
// Fix: https://github.com/quarkusio/quarkus/pull/57551
@Disabled("TODO Quarkus 4: OTel access log MDC timing - https://github.com/quarkusio/quarkus/pull/57551")
@QuarkusScenario
public class AccessLogIT {
    @QuarkusApplication(classes = { HelloResource.class })
    static final RestService app = new RestService()
            .withProperties("access-log.properties")
            .withProperty("quarkus.otel.traces.sampler.arg", "1.0d");

    @Test
    @Tag("QUARKUS-6555")
    public void testSpanAndTraceIdIsPresentInAccessLog() throws JacksonException {
        String endpointPath = "/hello/async";
        Response response = app.given().when().get(endpointPath);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(response.getBody().prettyPrint());

        String spanId = node.get("spanId").asText();
        String traceId = node.get("traceId").asText();

        app.logs().assertContains(endpointPath + " HTTP/1.1 traceId=" + traceId + " spanId=" + spanId);
    }
}
