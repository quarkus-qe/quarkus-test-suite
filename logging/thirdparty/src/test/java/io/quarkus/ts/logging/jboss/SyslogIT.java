package io.quarkus.ts.logging.jboss;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

@QuarkusScenario
public class SyslogIT {

    public static final Integer LENGTH_LIMIT = 128;
    /*
     * For manual testing:
     * podman run -p 8514:8514 -v $(pwd)/src/test/resources/syslog-ng.conf:/config/syslog-ng.conf:ro:z \
     * -e LOG_TO_STDOUT=true --name syslog --rm -it linuxserver/syslog-ng
     *
     * To manually check: echo "Hello syslog" | nc -w1 localhost 8514
     */
    @Container(image = "linuxserver/syslog-ng", port = 8514, expectedLog = ".*syslog-ng starting up")
    static RestService syslog = new RestService()
            .withProperty("LOG_TO_STDOUT", "true") //https://github.com/linuxserver/docker-syslog-ng/pull/30
            .withProperty("_ignored", "resource_with_destination::/config/|syslog-ng.conf");

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.log.syslog.max-length", LENGTH_LIMIT.toString())
            .withProperty("quarkus.log.syslog.endpoint", () -> syslog.getURI().toString())
            .withProperty("quarkus.profile", "syslog");

    @Test
    public void checkDefaultLogMinLevel() {
        Response response = app.given().when().get("/log");
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("Logs sent!", response.getBody().asString());
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
        String shorterMessage = "You won't believe it, but this message is exactly 64 chars long!";
        String longerMessage = shorterMessage.repeat(2);
        Assertions.assertEquals(64, shorterMessage.getBytes(StandardCharsets.UTF_8).length);
        Assertions.assertEquals(LENGTH_LIMIT, longerMessage.getBytes(StandardCharsets.UTF_8).length);

        // the order below is important. We must make sure, that the shorter message is sent after the longer
        app.given().when()
                .post("/log/static/info?message={message}",
                        longerMessage)
                .then().statusCode(204);
        app.given().when()
                .post("/log/static/info?message={message}",
                        shorterMessage)
                .then().statusCode(204);

        syslog.logs().assertContains(shorterMessage);
        syslog.getLogs()
                .forEach(line -> {
                    Assertions.assertFalse(line.contains(longerMessage));
                });
    }

}
