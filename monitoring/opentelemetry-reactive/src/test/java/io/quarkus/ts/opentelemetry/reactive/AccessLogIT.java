package io.quarkus.ts.opentelemetry.reactive;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

@QuarkusScenario
public class AccessLogIT {
    @QuarkusApplication(classes = { HelloResource.class })
    static final RestService app = new RestService()
            .withProperties("access-log.properties");

    @Test
    @Tag("QUARKUS-6555")
    public void testSpanAndTraceIdIsPresentInAccessLog() throws JsonProcessingException {
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
