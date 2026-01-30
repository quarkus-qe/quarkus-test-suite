package io.quarkus.ts.external.applications;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.GitRepositoryQuarkusApplication;

@DisabledOnNative(reason = "AoT is for JVM only")
@DisabledForJreRange(max = JRE.JAVA_24, disabledReason = "The generation of AoT file from ITs is available for Java 25+")
@QuarkusScenario
public class AoTQuickstartIT {
    public static final String QUICKSTART_DIRECTORY = "getting-started-reactive";
    public static final String AOT_PROPERTIES = "-Dquarkus.package.jar.appcds.enabled=true -Dquarkus.package.jar.appcds.use-aot=true";

    @GitRepositoryQuarkusApplication(repo = "https://github.com/quarkusio/quarkus-quickstarts.git", contextDir = QUICKSTART_DIRECTORY, branch = "development", mavenArgs = AOT_PROPERTIES
            + " -Dquarkus.platform.group-id=${QUARKUS_PLATFORM_GROUP-ID} -Dquarkus.platform.version=${QUARKUS_PLATFORM_VERSION}")
    static final RestService app = new RestService()
            .withProperty("-Xlog", "aot")
            .setAutoStart(false);

    @Test
    public void verifyAppGeneratedAndStartedWithAoTFile() {
        Path aotFilePath = getAoTFile();
        restartAppWithAoTFile(aotFilePath.toString());
        assertTrue(Files.exists(aotFilePath),
                "The AOT file should be generated when running the ITs");
        app.logs().assertContains("trying to map " + aotFilePath,
                "Opened AOT cache " + aotFilePath);

        verifyGetRequest();
    }

    @Test
    public void verifyAppStartingWithNonExistingAoTFile() {
        String nonExistingAoTFile = "nonExistingFile.aot";
        restartAppWithAoTFile(nonExistingAoTFile);
        app.logs().assertContains("trying to map " + nonExistingAoTFile,
                "An error has occurred while processing the AOT cache");

        verifyGetRequest();
    }

    private static Path getAoTFile() {
        return Paths.get(app.getServiceFolder().toAbsolutePath().toString(), QUICKSTART_DIRECTORY, "target", "quarkus-app",
                "app.aot");
    }

    private static void restartAppWithAoTFile(String aotFile) {
        app.stop();
        app.withProperty("-XX", "AOTCache=" + aotFile);
        app.start();
    }

    private static void verifyGetRequest() {
        app.given()
                .get("/hello")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }
}
