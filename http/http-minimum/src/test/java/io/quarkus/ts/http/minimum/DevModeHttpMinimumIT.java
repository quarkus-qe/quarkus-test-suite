package io.quarkus.ts.http.minimum;

import static org.hamcrest.CoreMatchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.DevModeQuarkusService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusVersion;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.test.utils.AwaitilityUtils;

@QuarkusScenario
@DisabledOnNative
@DisabledOnQuarkusVersion(version = "1\\..*", reason = "Continuous Testing was entered in 2.x")
public class DevModeHttpMinimumIT {

    static final String HELLO_IN_ENGLISH = "Hello, %s!";
    static final String HELLO_IN_SPANISH = "Hola, %s!";
    static final String WORLD = "World";

    @DevModeQuarkusApplication
    static DevModeQuarkusService app = new DevModeQuarkusService();

    @Test
    public void shouldDetectNewTests() {
        // At first, there are no tests annotated with @QuarkusTest
        app.logs().assertContains("Tests paused");
        // Now, we enable continuous testing via DEV UI
        app.enableContinuousTesting();
        // But there are no tests yet
        app.logs().assertContains("No tests found");
        // We add a new test
        app.copyFile("src/test/resources/HelloResourceTest.java.template", "src/test/java/HelloResourceTest.java");
        // So good so far!
        app.logs().assertContains("All 1 test is passing");
    }

    @Test
    public void shouldDetectChanges() {
        // Should say first Victor (the default name)
        app.given().get("/hello").then().statusCode(HttpStatus.SC_OK)
                .body("content", is(String.format(HELLO_IN_ENGLISH, WORLD)));

        // Modify default name to manuel
        app.modifyFile("src/main/java/io/quarkus/ts/http/minimum/HelloResource.java",
                content -> content.replace(HELLO_IN_ENGLISH, HELLO_IN_SPANISH));

        // Now, the app should say Manuel
        AwaitilityUtils.untilAsserted(
                () -> app.given().get("/hello").then().statusCode(HttpStatus.SC_OK)
                        .body("content", is(String.format(HELLO_IN_SPANISH, WORLD))));
    }
}
