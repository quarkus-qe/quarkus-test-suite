package io.quarkus.ts.monitoring.jfr;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.LookupService;
import io.quarkus.test.bootstrap.RestService;
import io.restassured.response.Response;

import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;

public abstract class AbstractJavaFlightRecorderAppInfoIT {

    public static final Path RECORDING_PATH = Path.of(System.getProperty("java.io.tmpdir"), "jfrRecording.jfr");

    @LookupService
    static RestService app;

    @Test
    public void checkAppInfo() throws IOException {
        app.start();

        String appName = executeGetRequestForInfo("/app-info/app-name");
        String appVersion = executeGetRequestForInfo("/app-info/app-version");

        app.stop();

        var appInfoRecordList = getRecordedEventsByName("quarkus.application");
        assertFalse(appInfoRecordList.isEmpty());

        RecordedEvent appInfoRecord = appInfoRecordList.get(0);
        assertEquals(appName, appInfoRecord.getString("name"));
        assertEquals(appVersion, appInfoRecord.getString("version"));
    }

    @Test
    public void checkRuntimeInfo() throws IOException {
        app.start();

        String quarkusVersion = executeGetRequestForInfo("/runtime-info/quarkus-version");
        String quarkusImageMode = executeGetRequestForInfo("/runtime-info/quarkus-image-mode");
        String quarkusProfiles = executeGetRequestForInfo("/runtime-info/quarkus-profiles");

        app.stop();

        var appInfoRecordList = getRecordedEventsByName("quarkus.runtime");
        assertFalse(appInfoRecordList.isEmpty());

        RecordedEvent appInfoRecord = appInfoRecordList.get(0);
        assertEquals(quarkusVersion, appInfoRecord.getString("version"));
        assertEquals(quarkusImageMode, appInfoRecord.getString("imageMode"));
        assertThat(quarkusProfiles, containsString(appInfoRecord.getString("profiles")));
    }

    @Test
    public void checkExtensionInfo() throws IOException {
        app.start();
        given().get("/hello").then().statusCode(HttpStatus.SC_OK);
        app.stop();

        List<String> installedExtensions = app.logs().forQuarkus().installedFeatures();

        var appInfoRecordList = getRecordedEventsByName("quarkus.extension");
        assertFalse(appInfoRecordList.isEmpty());

        // Iterate over all installed extension and chek if they were written to JFR file
        for (String installedExtension : installedExtensions) {
            boolean isExtensionPresentRecord = appInfoRecordList.stream()
                    .anyMatch(event -> installedExtension.equals(event.getString("name")));
            assertTrue(isExtensionPresentRecord, "Extension " + installedExtension + " not found in JFR record");
        }
    }

    private List<RecordedEvent> getRecordedEventsByName(String eventName) throws IOException {
        return RecordingFile.readAllEvents(RECORDING_PATH).stream()
                .filter(e -> e.getEventType().getName().equals(eventName))
                .toList();
    }

    private String executeGetRequestForInfo(String path) {
        Response appInfoResponse = given().get(path);
        assertEquals(HttpStatus.SC_OK, appInfoResponse.getStatusCode());
        String infoValue = appInfoResponse.body().asString();
        assertFalse(infoValue.isEmpty());
        return infoValue;
    }
}
