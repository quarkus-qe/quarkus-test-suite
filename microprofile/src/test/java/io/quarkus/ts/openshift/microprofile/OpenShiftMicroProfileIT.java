package io.quarkus.ts.openshift.microprofile;

import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusVersion;
import io.quarkus.test.scenarios.annotations.EnabledIfOpenShiftScenarioPropertyIsTrue;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.when;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;

@OpenShiftScenario
@EnabledIfOpenShiftScenarioPropertyIsTrue
@DisabledOnQuarkusVersion(version = "1\\.3\\..*", reason = "https://github.com/quarkusio/quarkus/pull/7987")
public class OpenShiftMicroProfileIT extends MicroProfileIT {
    private static final int EXPECTED_SPANS_SIZE = 3;
    private static final int EXPECTED_DATA_SIZE = 1;

    @Test
    public void verifyTracesInJaegerTest() {
        // the tracer inside the application doesn't send traces to the Jaeger server immediately,
        // they are batched, so we need to wait a bit
        await().atMost(TIMEOUT_SEC, TimeUnit.SECONDS).untilAsserted(() -> {
            when()
                    .get(jaeger.getTraceUrl() + "?service=" + SERVICE_NAME)
                    .then()
                    .statusCode(HttpURLConnection.HTTP_OK)
                    .log().body()
                    .log().status()
                    .body("data", hasSize(EXPECTED_DATA_SIZE))
                    .body("data[0].spans", hasSize(EXPECTED_SPANS_SIZE))
                    .body("data[0].spans.operationName", hasItems(
                            "GET:io.quarkus.ts.openshift.microprofile.ClientResource.get",
                            "GET",
                            "GET:io.quarkus.ts.openshift.microprofile.HelloResource.get"
                    ))
                    .body("data[0].spans.logs.fields.value.flatten()", hasItems(
                            "ClientResource called",
                            "HelloResource called",
                            "HelloService called",
                            "HelloService async processing"
                    ))
                    .body("data[0].spans.find { "
                            +
                            "it.operationName == 'GET:io.quarkus.ts.openshift.microprofile.ClientResource.get' }.tags.collect"
                            +
                            " { \"${it.key}=${it.value}\".toString() }", hasItems(
                            "span.kind=server",
                            "component=jaxrs",
                            "http.url=" + app.getHost() + ":80/client",
                            "http.method=GET",
                            "http.status_code=200"
                    ))
                    .body("data[0].spans.find {"
                            +
                            " it.operationName == 'GET' }.tags.collect { \"${it.key}=${it.value}\".toString() }", hasItems(
                            "span.kind=client",
                            "component=jaxrs",
                            "http.url=http://localhost:8080/hello",
                            "http.method=GET",
                            "http.status_code=200"
                    ))
                    .body("data[0].spans.find { "
                            +
                            "it.operationName == 'GET:io.quarkus.ts.openshift.microprofile.HelloResource.get' }.tags.collect "
                            +
                            "{ \"${it.key}=${it.value}\".toString() }", hasItems(
                            "span.kind=server",
                            "component=jaxrs",
                            "http.url=http://localhost:8080/hello",
                            "http.method=GET",
                            "http.status_code=200"
                    ));
        });
    }
}
