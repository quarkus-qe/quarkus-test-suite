package io.quarkus.ts.logging.jboss;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/2024")
public class SyslogIT {

    /*
     * For manual testing:
     * podman run -p 8514:514 \
     * -v $(pwd)/src/test/resources/syslog-ng.conf:/etc/syslog-ng/syslog-ng.conf:z --rm -it balabit/syslog-ng
     */
    @Container(image = "balabit/syslog-ng", port = 514, expectedLog = "syslog-ng")
    static RestService syslog = new RestService()
            .withProperty("_ignored", "resource_with_destination::/etc/syslog-ng/|syslog-ng.conf");

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.profile", "syslog")
            .withProperty("quarkus.log.syslog.max-length", "64")
            .withProperty("quarkus.log.syslog.endpoint", () -> syslog.getURI().toString());

    @Test
    public void checkDefaultLogMinLevel() {
        app.given().when().get("/log").then().statusCode(204);

        syslog.logs().assertContains("Fatal log example");

        syslog.logs().assertContains("Error log example");
        syslog.logs().assertContains("Warn log example");
        syslog.logs().assertContains("Info log example");

        // the value of minimum logging level overrides the logging level
        syslog.logs().assertDoesNotContain("Debug log example");
        syslog.logs().assertDoesNotContain("Trace log example");
    }

    @Test
    @Tag("https://issues.redhat.com/browse/QUARKUS-4531")
    public void logBigMessage() {
        String shorterMessage = "Relatively long message";
        app.given().when()
                .post("/log/static/info?message={message}", shorterMessage)
                .then().statusCode(204);
        syslog.logs().assertContains(shorterMessage);
    }

    @Test
    @Tag("https://issues.redhat.com/browse/QUARKUS-4531")
    public void filterBigMessage() {
        String longerMessage = "Message, which is very long and is not expected to fit into 64 bytes";
        app.given().when()
                .post("/log/static/info?message={message}",
                        longerMessage)
                .then().statusCode(204);

        syslog.logs().assertDoesNotContain(longerMessage);
    }

}
