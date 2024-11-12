package io.quarkus.ts.http.advanced.reactive;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

/**
 * Test makes sure AsyncFile gets closed, coverage triggered by https://github.com/quarkusio/quarkus/issues/41811
 */
@QuarkusScenario
@DisabledOnNative(reason = "To save resources on CI")
@DisabledOnOs(value = OS.WINDOWS, disabledReason = "No lsof command on Windows")
class DownloadResourceIT {
    @QuarkusApplication(classes = { DownloadResource.class }, properties = "oidcdisable.properties")
    static RestService app = new RestService();

    @Test
    void ensureAsyncFileGetsClosed() throws IOException {
        Response response = app.given()
                .when().post("/download/create")
                .then()
                .statusCode(200)
                .extract().response();
        String file = response.getBody().asString();

        app.given()
                .when().get("/download")
                .then()
                .statusCode(200);

        ProcessBuilder lsofBuilder = new ProcessBuilder("lsof", file);
        Process lsofProcess = lsofBuilder.start();
        String lsofOutput = new BufferedReader(new InputStreamReader(lsofProcess.getInputStream())).lines()
                .collect(Collectors.joining("\n"));

        app.given()
                .when().delete("/download/delete")
                .then()
                .statusCode(204);

        assertEquals(0, lsofOutput.length(), "AsyncFile is not closed, details:\n" + lsofOutput + "\n");
    }

}
