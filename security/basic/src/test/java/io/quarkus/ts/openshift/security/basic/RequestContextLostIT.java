package io.quarkus.ts.openshift.security.basic;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.SocketTimeoutException;

import org.apache.http.params.CoreConnectionPNames;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;

@Tag("QUARKUS-7806")
@QuarkusScenario
public class RequestContextLostIT {

    private static final int SOCKET_TIMEOUT = 500;
    private static final int WAIT_TIME = 1000;
    private static final int DELAY = 2000;

    @QuarkusApplication
    static RestService app = new RestService();

    @Test
    void testThatRequestContextIsNotLostWhenConnectionTimeoutBeforeEndOfOperation() throws InterruptedException {

        // Setting the socket timeout to be lower, then wait_time
        var config = RestAssured.config()
                .httpClient(HttpClientConfig.httpClientConfig().setParam(CoreConnectionPNames.SO_TIMEOUT, SOCKET_TIMEOUT));

        assertThrows(SocketTimeoutException.class, () -> {
            RestAssured
                    .given()
                    .config(config)
                    .auth().basic("albert", "E!nst3iN")
                    .queryParam("wait_time", WAIT_TIME).when().post("/lost-context/delay");
        });

        // There is need to wait for RequestContextLost#delay to finish before checking the log.
        // It's because we access HttpHeaders after the timeout passed by `timeout` query parameter
        Thread.sleep(DELAY);

        app.logs().assertDoesNotContain(
                "jakarta.enterprise.context.ContextNotActiveException: RequestScoped context was not active");
    }
}
