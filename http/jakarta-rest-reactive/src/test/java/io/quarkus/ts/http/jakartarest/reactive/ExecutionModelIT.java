package io.quarkus.ts.http.jakartarest.reactive;

import static org.hamcrest.CoreMatchers.equalTo;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-1075")
@QuarkusScenario
public class ExecutionModelIT {
    public static final String THREAD_BLOCKED = "io.vertx.core.VertxException: Thread blocked";

    @QuarkusApplication
    static RestService app = new RestService().withProperties("execution-model.properties");

    @BeforeEach
    void beforeEach() {
        app.start();
    }

    @AfterEach
    void afterEach() {
        app.stop();
    }

    @Test
    public void shouldNotBlockIOThread() {
        callRequest("/execution-model/imperative");
        app.logs().assertDoesNotContain(THREAD_BLOCKED);
    }

    @ParameterizedTest
    @ValueSource(strings = { "uni", "multi", "completion-stage" })
    public void shouldBlockIOThread(String path) {
        callRequest("/execution-model/reactive/" + path);
        app.logs().assertContains(THREAD_BLOCKED);
    }

    private void callRequest(String path) {
        app.given().get(path).then().statusCode(HttpStatus.SC_OK).body(equalTo(ExecutionModelResource.RESPONSE));
    }
}
