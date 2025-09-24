package io.quarkus.ts.sqldb.sqlapp;

import static org.hamcrest.Matchers.containsString;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.restassured.response.Response;

@Tag("QUARKUS-6164")
@QuarkusScenario
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CustomDriverRegistrationIT {

    @DevModeQuarkusApplication
    static RestService app = new RestService();

    @Test
    @Order(1)
    public void checkThatDriverIsRegisteredAfterStartUp() {
        getDriverList();
    }

    @Test
    @Order(2)
    public void checkThatDriverIsRegisteredAfterReload() {
        triggerDevModeReloadAndWait();
        getDriverList();
    }

    private void getDriverList() {
        app.given()
                .when()
                .get("/drivers/list")
                .then()
                .statusCode(200).body(containsString("io.quarkus.ts.sqldb.sqlapp.driver.TestJdbcDriver"));
    }

    private void triggerDevModeReloadAndWait() {
        Path driverRegistrationResource = Path.of("src", "main", "java", "io", "quarkus", "ts", "sqldb", "sqlapp",
                "DriverRegistrationResource.java");
        Path file = app.getServiceFolder().resolve(driverRegistrationResource);
        try {
            String content = FileUtils.readFileToString(file.toFile(), StandardCharsets.UTF_8);
            content = content.replace("initial value", "changedVariable");

            FileUtils.writeStringToFile(file.toFile(), content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Assertions.fail("Could not load file " + file, e);
        }

        Awaitility.await()
                .pollInterval(1, TimeUnit.SECONDS)
                .atMost(10, TimeUnit.SECONDS).until(() -> {
                    // Request trigger live reload, but need to wait for it,
                    // otherwise the response returning the values before the reload
                    Response response = app.given().when().get("/drivers/list");
                    return response.statusCode() == 200
                            && app.getLogs().stream().anyMatch(line -> line.contains("Live reload total time"));
                });
    }
}
