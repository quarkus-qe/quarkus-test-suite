package io.quarkus.ts.monitoring.jfr;

import static io.quarkus.ts.monitoring.jfr.JfrUtils.RECORDING_PATH;
import static io.quarkus.ts.monitoring.jfr.JfrUtils.getRecordedEventsByName;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnSemeruJdk;
import io.quarkus.test.services.QuarkusApplication;

import jdk.jfr.consumer.RecordedEvent;

@QuarkusScenario
@DisabledOnSemeruJdk(reason = "Semeru don't have full support for JFR yet")
public class JavaFlightRecorderRestInfoIT {

    private static final String ENDPOINT_PATH = "/hello";

    @QuarkusApplication
    static final RestService app = new RestService()
            .onPreStop(JfrUtils::dumpJfrRecording)
            .withProperty("-XX", "StartFlightRecording=dumponexit=true,filename=" + RECORDING_PATH)
            .setAutoStart(false);

    @BeforeEach
    public void startApp() {
        app.start();
    }

    @AfterEach
    public void cleanJfrRecording() throws IOException {
        Files.deleteIfExists(RECORDING_PATH);
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
    public void checkPostEventRecord() throws IOException {
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
}
