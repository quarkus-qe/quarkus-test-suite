package io.quarkus.ts.lifecycle;

import java.net.ConnectException;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

@QuarkusScenario
@Tag("QUARKUS-7345")
public class NoServerErrorsIT {
    private final CountDownLatch latch = new CountDownLatch(1);
    @QuarkusApplication
    static RestService app = new RestService().setAutoStart(false);

    public static Stream<Arguments> getParameters() {
        return Stream.of(
                Arguments.of("/slow", true),
                Arguments.of("/slow/reactive", true),
                Arguments.of("/slow", false),
                Arguments.of("/slow/reactive", false));
    }

    @ParameterizedTest
    @MethodSource("getParameters")
    public void checkStatus(String endpoint, boolean http2) throws InterruptedException {
        app.withProperty("quarkus.http.http2", String.valueOf(http2));
        app.start();
        Assertions.assertEquals(200, app.given().get(endpoint).statusCode());
        Thread thread = new Thread(() -> accessEndpoint(endpoint), "data access");
        thread.start();
        latch.await();
        Thread.sleep(2_000);
        app.stop();
        thread.join();
        try {
            app.given().get(endpoint);
            Assertions.fail("The connection should be rejected!");
        } catch (Exception ex) {
            Assertions.assertEquals(ConnectException.class, ex.getClass());
            Assertions.assertTrue(ex.getMessage().contains("Connection refused"));
        }
    }

    public void accessEndpoint(String method) {
        latch.countDown();

        Response response = app.given().get(method);
        Assertions.assertNotEquals(503, response.getStatusCode());
    }
}
