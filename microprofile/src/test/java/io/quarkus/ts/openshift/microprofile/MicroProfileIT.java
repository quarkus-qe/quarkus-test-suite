package io.quarkus.ts.openshift.microprofile;

import io.quarkus.test.bootstrap.JaegerService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.JaegerContainer;
import io.quarkus.test.services.QuarkusApplication;
import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.awaitility.Awaitility.with;
import static org.hamcrest.CoreMatchers.is;

@QuarkusScenario
public class MicroProfileIT {

    protected static final String SERVICE_NAME = "test-traced-service";
    protected static final int TIMEOUT_SEC = 59;
    protected static final int POLL_DELAY_SEC = 10;

    @JaegerContainer
    static JaegerService jaeger = new JaegerService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.jaeger.service-name", SERVICE_NAME)
            .withProperty("quarkus.jaeger.endpoint", jaeger::getRestUrl);

    @Test
    public void helloTest() {
        with().pollInterval(Duration.ofSeconds(1)).and()
                .with().pollDelay(Duration.ofSeconds(POLL_DELAY_SEC)).await()
                .atLeast(Duration.ofSeconds(1))
                .atMost(TIMEOUT_SEC, TimeUnit.SECONDS)
                .with()
                .untilAsserted(() -> {
                    app.given().log().uri().when()
                            .get("/client")
                            .then()
                            .statusCode(HttpURLConnection.HTTP_OK)
                            .log().body()
                            .log().status()
                            .body(is("Client got: Hello, World!"));
                });
    }

    @Test
    @Disabled("https://issues.redhat.com/browse/QUARKUS-697")
    public void fallbackTest() {
        with().pollInterval(Duration.ofSeconds(1)).and()
                .with().pollDelay(Duration.ofSeconds(POLL_DELAY_SEC)).await()
                .atLeast(Duration.ofSeconds(1))
                .atMost(TIMEOUT_SEC, TimeUnit.SECONDS)
                .with()
                .untilAsserted(() -> {
                    app.given().log().uri().when()
                            .get("/client/fallback")
                            .then()
                            .log().body()
                            .log().status()
                            .statusCode(HttpURLConnection.HTTP_OK)
                            .body(is("Client got: Fallback"));
                });
    }

}
