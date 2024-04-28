package io.quarkus.ts.messaging.amqpreactive;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.AmqService;
import io.quarkus.test.bootstrap.JaegerService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.AmqContainer;
import io.quarkus.test.services.JaegerContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.containers.model.AmqProtocol;
import io.restassured.response.Response;

@QuarkusScenario
public class ProdAmqpReactiveIT extends BaseAmqpReactiveIT {

    @JaegerContainer(expectedLog = "\"Health Check state change\",\"status\":\"ready\"")
    static final JaegerService jaeger = new JaegerService();

    @AmqContainer(image = "${amqbroker.image}", protocol = AmqProtocol.AMQP)
    static AmqService amq = new AmqService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("amqp-host", amq::getAmqpHost)
            .withProperty("amqp-port", () -> "" + amq.getPort())
            .withProperty("quarkus.otel.exporter.otlp.traces.endpoint", jaeger::getCollectorUrl);

    private Response resp;

    @Test
    public void testContextPropagation() {
        int pageLimit = 10;
        String operationName = "GET /price";
        String lookback = "1h";
        String serviceName = "messaging-amqp-reactive";
        await().atMost(5, TimeUnit.SECONDS).pollInterval(Duration.ofSeconds(1)).untilAsserted(() -> {
            thenRetrieveTraces(operationName, lookback, pageLimit, serviceName);
            thenTraceSpanSizeMustBe(is(1));
            verifyStandardSourceCodeAttributesArePresent(operationName);

        });
    }

    private void thenRetrieveTraces(String operationName, String lookback, int pageLimit, String serviceName) {
        resp = app.given().when()
                .queryParam("operation", operationName)
                .queryParam("lookback", lookback)
                .queryParam("limit", pageLimit)
                .queryParam("service", serviceName)
                .get(jaeger.getTraceUrl())
                .then().log().all().extract().response();
    }

    private void thenTraceSpanSizeMustBe(Matcher<?> matcher) {
        resp.then().body("data[0].spans.size()", matcher);
    }

    private void verifyStandardSourceCodeAttributesArePresent(String operationName) {
        verifyAttributeValue(operationName, "code.namespace", PriceResource.class.getName());
        verifyAttributeValue(operationName, "code.function", "price");
    }

    private void verifyAttributeValue(String operationName, String attributeName, String attributeValue) {
        resp.then().body(getGPathForOperationAndAttribute(operationName, attributeName), is(attributeValue));
    }

    private static String getGPathForOperationAndAttribute(String operationName, String attribute) {
        return String.format("data[0].spans.find { it.operationName == '%s' }.tags.find { it.key == '%s' }.value",
                operationName, attribute);
    }

}
