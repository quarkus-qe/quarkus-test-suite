package io.quarkus.ts.http.advanced.reactive;

import static org.hamcrest.Matchers.containsString;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@Tag("https://github.com/quarkusio/quarkus/pull/47378")
@DisabledOnNative(reason = "Test RuntimeClassLoader is enough in JVM mode")
public class ResourceFinderIT {
    private static final String TEXT_FILE_DIRECTORY = "resource-finder-dir/";
    private static final String EXPECTED_TEXT_FILE_PATH = TEXT_FILE_DIRECTORY + "resource_finder_dir.txt";

    private static final String CLASS_MATCHER = ".*.class";
    private static final String TEXT_FILE_MATCHER = ".*.txt";

    private static final String expectedClassFilePath = ResourcesFinderResource.class.getName().replace('.', '/');

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperties("oidcdisable.properties");

    @Test
    public void testIfClassResourceIsPresentOnFullPath() {
        resourceRequest("io/quarkus/ts/http/advanced/reactive/", CLASS_MATCHER, expectedClassFilePath);
    }

    @Test
    public void testIfClassResourceIsPresentOnPartialPath() {
        resourceRequest("io/quarkus/ts/http/", CLASS_MATCHER, expectedClassFilePath);
    }

    @Test
    public void testIfFileResourceIsPresent() {
        resourceRequest(TEXT_FILE_DIRECTORY, TEXT_FILE_MATCHER, EXPECTED_TEXT_FILE_PATH);
    }

    private void resourceRequest(String resourcePath, String lookupPattern, String expectedFile) {
        app.given()
                .param("resourcePath", resourcePath)
                .param("lookupPattern", lookupPattern)
                .get("/api/resource-finder/find")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(containsString(expectedFile));
    }
}
