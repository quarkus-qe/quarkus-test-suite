package io.quarkus.ts.http.restclient.reactive;

import static io.quarkus.ts.http.restclient.reactive.inputstream.InputStreamClientResource.CONTENT;
import static io.quarkus.ts.http.restclient.reactive.inputstream.RequestScopedInputStream.MAX_ITERATIONS;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import java.nio.charset.StandardCharsets;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QQE-614")
@QuarkusScenario
public class InputStreamRestClientIT {

    private static final String CONTENT_LENGTH = String.valueOf(CONTENT.getBytes(StandardCharsets.UTF_8).length);

    @QuarkusApplication(properties = "modern.properties")
    static RestService app = new RestService();

    @Test
    public void sendInputStream() {
        app.given().get("/client/input-stream/count")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is(CONTENT_LENGTH));
    }

    @Test
    public void sendInputStreamReactive() {
        app.given().get("/client/input-stream/uni-count")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is(CONTENT_LENGTH));
    }

    @Test
    public void sendRequestScopedInputStream() {
        // RequestScopedInputStream returns A for the first MAX_ITERATIONS reads and then -1,
        // so the body is exactly MAX_ITERATIONS bytes. InputStreamResource.count() returns the
        // number of bytes it received, so the response equals MAX_ITERATIONS only if the whole stream was sent.
        // it's called twice to verify that every request gets its own stream instance.
        for (int i = 0; i < 2; i++) {
            app.given().get("/client/input-stream/request-scoped/count")
                    .then()
                    .statusCode(HttpStatus.SC_OK)
                    .body(is(String.valueOf(MAX_ITERATIONS)));
        }
    }

    @Test
    public void sendRequestScopedInputStreamReactive() {
        // the stream must be read on a worker thread even when the client is called from the event loop
        for (int i = 0; i < 2; i++) {
            app.given().get("/client/input-stream/request-scoped/uni-count")
                    .then()
                    .statusCode(HttpStatus.SC_OK)
                    .body(is(String.valueOf(MAX_ITERATIONS)));
        }
    }

    @Test
    public void sendInputStreamWithConfiguredHeaders() {
        app.given().get("/client/input-stream/headers")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(containsString("x-static-header=static-header-value"))
                .body(containsString("content-type=application/octet-stream"))
                .body(containsString("length=" + CONTENT_LENGTH));
    }
}
