package io.quarkus.ts.monitoring.jfr;

import static io.quarkus.ts.monitoring.jfr.JfrUtils.RECORDING_PATH;
import static io.quarkus.ts.monitoring.jfr.JfrUtils.getRecordedEventsByName;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.runtime.ImageMode;
import io.quarkus.test.bootstrap.LookupService;
import io.quarkus.test.bootstrap.RestService;
import io.restassured.response.Response;

import jdk.jfr.consumer.RecordedEvent;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractJavaFlightRecorderAppInfoIT {

    @LookupService
    static RestService app;

    @AfterEach
    public void cleanJfrRecording() throws IOException {
        Files.deleteIfExists(RECORDING_PATH);
    }

    @Test
    @Order(1)
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
    @Order(2)
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
    @Order(3)
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

    @Tag("QUARKUS-6553")
    @Test
    @Order(4)
    public void checkRuntimeInfoMultipleProfile() throws IOException {
        app.withProperty("quarkus.profile", "prod,myProfile");
        app.start();

        String quarkusVersion = executeGetRequestForInfo("/runtime-info/quarkus-version");
        String quarkusImageMode = executeGetRequestForInfo("/runtime-info/quarkus-image-mode");
        String quarkusProfiles = executeGetRequestForInfo("/runtime-info/quarkus-profiles");

        app.stop();

        // Determinate which image mode the record should contain. For native ImageMode.NATIVE_BUILD should not be present.
        String expectedImageMode = quarkusImageMode.contains("NATIVE") ? ImageMode.NATIVE_RUN.name() : ImageMode.JVM.name();

        // Ensuring that runtime profiles are same as profile set by Quarkus property
        assertEquals("myProfile,prod", quarkusProfiles, "Quarkus runtime profiles not match the set profiles");

        var appInfoRecordList = getRecordedEventsByName("quarkus.runtime");
        assertFalse(appInfoRecordList.isEmpty());

        RecordedEvent appInfoRecord = appInfoRecordList.get(0);
        assertEquals(quarkusVersion, appInfoRecord.getString("version"), "Quarkus version in record not match runtime version");
        assertEquals(expectedImageMode, appInfoRecord.getString("imageMode"),
                "Quarkus image mode in record not match runtime image mode");
        assertThat("Quarkus profiles in record not match runtime profiles", quarkusProfiles,
                containsString(appInfoRecord.getString("profiles")));
    }

    private String executeGetRequestForInfo(String path) {
        Response appInfoResponse = given().get(path);
        assertEquals(HttpStatus.SC_OK, appInfoResponse.getStatusCode());
        String infoValue = appInfoResponse.body().asString();
        assertFalse(infoValue.isEmpty());
        return infoValue;
    }
}
