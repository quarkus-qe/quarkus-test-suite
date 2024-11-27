package io.quarkus.ts.http.undertow;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.Service;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class UndertowMissingServletClassIT {

    // if you rename the field 'missingServletTest', please also adjust test.properties
    @QuarkusApplication(builder = WithMissingServlet.class)
    static RestService missingServletTest = new RestService().setAutoStart(false);

    @Tag("https://github.com/quarkusio/quarkus/issues/44063")
    @Test
    void verifyUndertowIgnoreServletClassMissing() {
        assertDoesNotThrow(() -> missingServletTest.start(),
                "The app should start without any issue");
        missingServletTest.logs().assertDoesNotContain("Local name must not be null");
    }

    public static void replaceForInvalidXML(Service service) {
        if (service instanceof RestService) {
            RestService restService = (RestService) service;
            Path invalidWebXml = Path.of("src/test/resources/META-INF/invalid-web.xml");

            Path targetWebXml = restService.getServiceFolder().resolve("META-INF/web.xml");
            try {
                Files.createDirectories(targetWebXml.getParent());
                Files.copy(invalidWebXml, targetWebXml, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Failed to replace web.xml with invalid version", e);
            }
        } else {
            throw new IllegalArgumentException("Service is not an instance of RestService");
        }

    }

}
