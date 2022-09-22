package io.quarkus.ts.qute;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.DevModeQuarkusService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.test.utils.AwaitilityUtils;
import io.restassured.response.Response;

@QuarkusScenario
public class DevModeQuteReactiveIT {
    private static final String FILE = "src/main/resources/templates/basic.html";
    private static final String RESERVE = "src/main/resources/templates/basic.html.bck";

    @DevModeQuarkusApplication
    static DevModeQuarkusService app = new DevModeQuarkusService();

    @BeforeAll
    static void beforeAll() {
        app.copyFile(FILE, RESERVE);
    }

    @Test
    void changeTemplate() {
        Response was = app.given().get("/basic");
        assertEquals("<html>\n    <p>This page is rendered by Quarkus</p>\n</html>\n", was.body().asString());
        app.modifyFile(FILE, string -> string.replace("This page is rendered", "This page is being edited"));
        AwaitilityUtils.untilAsserted(() -> {
            Response changed = app.given().get("/basic");
            assertEquals("<html>\n    <p>This page is being edited by Quarkus</p>\n</html>\n", changed.body().asString());
        });
        app.logs().assertContains("File change detected");
        app.logs().assertContains("Restarting quarkus due to changes in basic.html");

        assertEquals("<html>\n    <p>This page is being edited by basic engine</p>\n</html>\n",
                app.given().get("/engine/basic").body().asString());
    }

    @AfterAll
    static void afterAll() {
        app.copyFile(RESERVE, FILE);
    }
}
