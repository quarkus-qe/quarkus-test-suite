package io.quarkus.ts.http.restclient.vanilla;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusVersion;
import io.quarkus.test.services.QuarkusApplication;

@DisabledOnQuarkusVersion(version = "3.2.9.Final", reason = "Fixed in 3.2.10.Final")
@QuarkusScenario
public class VanillaReactiveRestClientIT {
    private static final String CONTAINER_FILTER_ERROR_TEXT = "CDI: programmatic lookup problem detected";

    @QuarkusApplication
    static RestService app = new RestService();

    @Test
    @Tag("https://github.com/quarkusio/quarkus/issues/31024")
    public void restClientContainerFilterTest() {
        List<String> logs = app.getLogs();

        // app produces error log (not stack trace) after startup in its log if issue in not fixed,
        // search for this error text
        for (String log : logs) {
            if (log.contains(CONTAINER_FILTER_ERROR_TEXT)) {
                fail("Detected failure-indicating text in app's log. Issue https://github.com/quarkusio/quarkus/issues/31024 might not be fixed.");
            }
        }
    }
}
