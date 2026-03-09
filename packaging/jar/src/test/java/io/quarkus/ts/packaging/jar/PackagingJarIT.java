package io.quarkus.ts.packaging.jar;

import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
class PackagingJarIT {
    @QuarkusApplication
    static RestService app = new RestService();

    @Test
    void packagingTest() throws IOException {
        app.given()
                .get("/hello")
                .then()
                .statusCode(SC_OK)
                .body(is("Hello from jar packaged resource"));

        Path targetDir = app.getServiceFolder().resolve("../..").toAbsolutePath();
        AtomicBoolean jarPackageFound = new AtomicBoolean(false);
        try (Stream<Path> files = Files.list(targetDir)) {
            files.forEach(file -> {
                if (file.getFileName().toString().matches("packaging.*\\.jar")) {
                    jarPackageFound.set(true);
                }
            });
        }
        assertTrue(jarPackageFound.get(), "There should be a jar file built in packaging:jar app's target dir: " + targetDir);
    }
}
