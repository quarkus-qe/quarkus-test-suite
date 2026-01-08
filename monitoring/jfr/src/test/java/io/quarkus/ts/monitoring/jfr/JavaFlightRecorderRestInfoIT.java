package io.quarkus.ts.monitoring.jfr;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnSemeruJdk;
import io.quarkus.test.services.QuarkusApplication;

import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;

@QuarkusScenario
@DisabledOnSemeruJdk(reason = "Semeru don't have full support for JFR yet")
public class JavaFlightRecorderRestInfoIT {

    public static final Path RECORDING_PATH = Path.of(System.getProperty("java.io.tmpdir"), "jfrRecording.jfr");
    private static final String ENDPOINT_PATH = "/hello";

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("-XX:StartFlightRecording", "dumponexit=true,filename=" + RECORDING_PATH)
            .setAutoStart(false);

    @BeforeEach
    public void startApp() {
        app.start();
    }

    @Test
    public void checkGetEventRecord() throws IOException {
        given().get(ENDPOINT_PATH).then().statusCode(HttpStatus.SC_OK);
        app.stop();

        var appInfoRecordList = getRecordedEventsByName("quarkus.Rest");
        // Only one request was executed so the one request should be recorded
        assertEquals(1, appInfoRecordList.size());

        RecordedEvent appInfoRecord = appInfoRecordList.get(0);
        assertEquals(ENDPOINT_PATH, appInfoRecord.getString("uri"), "The endpoint path in record not match the requested path");
        assertEquals("GET", appInfoRecord.getString("httpMethod"),
                "The http method in record not match the http method which was used");
        assertEquals(HelloResource.class.getName(), appInfoRecord.getString("resourceClass"));
        assertEquals("getHello", appInfoRecord.getString("resourceMethod"));
    }

    @Test
    public void checkRuntimeInfo() throws IOException {
        given().post(ENDPOINT_PATH).then().statusCode(HttpStatus.SC_OK);
        app.stop();

        var appInfoRecordList = getRecordedEventsByName("quarkus.Rest");
        // Only one request was executed so the one request should be recorded
        assertEquals(1, appInfoRecordList.size());

        RecordedEvent appInfoRecord = appInfoRecordList.get(0);
        assertEquals(ENDPOINT_PATH, appInfoRecord.getString("uri"), "The endpoint path in record not match the requested path");
        assertEquals("POST", appInfoRecord.getString("httpMethod"),
                "The http method in record not match the http method which was used");
        assertEquals(HelloResource.class.getName(), appInfoRecord.getString("resourceClass"));
        assertEquals("postHello", appInfoRecord.getString("resourceMethod"));
    }

    private List<RecordedEvent> getRecordedEventsByName(String eventName) throws IOException {
        return RecordingFile.readAllEvents(RECORDING_PATH).stream()
                .filter(e -> e.getEventType().getName().equals(eventName))
                .toList();
    }
}
