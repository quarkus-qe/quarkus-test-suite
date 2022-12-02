package io.quarkus.ts.http.jaxrs.reactive;

import java.util.function.Predicate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario
public class OpenShiftExecutionModelIT extends ExecutionModelIT {
    //TODO https://github.com/quarkusio/quarkus/issues/29642
    // investigate: when FILE_SIZE is set to 99999999, we are getting a
    // "java.lang.OutOfMemoryError: Array allocation too large." on OCP-native
    private static final int FILE_SIZE = 9999999;

    @Test
    public void bigCompression() {
        String path = "/compression/big/payload?bodyCharSize=" + FILE_SIZE;
        app.given()
                .when()
                .get(path).then().statusCode(200)
                .header("Content-Encoding", "gzip");

        Predicate<String> containsError = line -> line.contains("io.vertx.core.VertxException: Thread blocked");
        Assertions.assertFalse(app.getLogs().stream().anyMatch(containsError));
    }
}
