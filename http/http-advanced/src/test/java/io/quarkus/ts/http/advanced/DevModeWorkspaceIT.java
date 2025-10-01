package io.quarkus.ts.http.advanced;

import static io.quarkus.test.utils.AwaitilityUtils.AwaitilitySettings.usingTimeout;
import static java.time.Duration.ofSeconds;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.test.utils.AwaitilityUtils;
import io.quarkus.test.utils.FileUtils;
import io.restassured.response.Response;

@Tag("QUARKUS-6247")
@QuarkusScenario
public class DevModeWorkspaceIT {

    @DevModeQuarkusApplication(properties = "keycloakless.properties", classes = { PathSpecificHeadersResource.class })
    static final RestService app = new RestService();

    @Test
    public void workSpaceExists() {
        app.given().when().get("/q/dev-ui/workspace").then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void workSpaceContainsText() {
        var pageURL = app.getURI(Protocol.HTTP).withPath("/q/dev-ui/workspace").toString();
        try (Playwright playwright = Playwright.create()) {
            try (Browser browser = playwright.firefox().launch()) {
                Page page = browser.newContext().newPage();
                page.navigate(pageURL);
                ElementHandle element = page.waitForSelector("#code");
                String code = element.getAttribute("value");
                Assertions.assertTrue(code.startsWith("package io.quarkus.ts.http.advanced;"),
                        "The code doesn't contain the expected value: " + code);
            }
        }
    }

    @Test
    public void workSpaceCanBeEdited() {
        app.given().when().get("/api/filter/any")
                .then().statusCode(HttpStatus.SC_OK).body(containsString("ok"));
        app.given().when().get("/api/filter/this")
                .then().statusCode(HttpStatus.SC_NOT_FOUND);

        var pageURL = app.getURI(Protocol.HTTP).withPath("/q/dev-ui/workspace").toString();
        try (Playwright playwright = Playwright.create()) {
            try (Browser browser = playwright.firefox().launch()) {
                Page page = browser.newContext().newPage();
                page.navigate(pageURL);

                // Check, that the correct file was opened
                ElementHandle textArea = page.waitForSelector("#code");
                String code = textArea.getAttribute("value");
                Assertions.assertTrue(code.contains("PathSpecificHeadersResource"),
                        "The code doesn't contain the expected value: " + code);

                //Edit the file and save the changes
                code = code.replace("@Path(\"/any\")", "@Path(\"/this\")");
                ElementHandle editor = textArea.waitForSelector(".cm-content");
                editor.fill(code);
                ElementHandle save = page.waitForSelector(".mainMenuBarButtons > vaadin-button:nth-child(1)");
                save.click();

                AwaitilityUtils.untilAsserted(() -> {
                    Path file = app.getServiceFolder()
                            .resolve(Path.of("src", "main", "java", "io", "quarkus", "ts", "http", "advanced",
                                    "PathSpecificHeadersResource.java"));
                    String content = FileUtils.loadFile(file.toFile());
                    Assertions.assertTrue(content.contains("@Path(\"/this\")"),
                            file + " wasn't edited:" + System.lineSeparator() + content);
                }, usingTimeout(ofSeconds(5)));

                // Sometimes the app returns the old results, so let's refresh the page
                page.reload();
                //check, that the changes are reflected in ui as well
                textArea = page.waitForSelector("#code");
                textArea.getAttribute("value");
                Assertions.assertTrue(code.contains("@Path(\"/this\")"),
                        "The code doesn't contain the expected value: " + code);
                Awaitility.await()
                        .pollInterval(1, TimeUnit.SECONDS)
                        .atMost(30, TimeUnit.SECONDS).until(() -> {
                            // the app reload is triggered by http request
                            Response response = app.given().when().get("/api/filter/this");
                            return response.statusCode() == 200
                                    && app.getLogs().stream().anyMatch(line -> line.contains("Live reload total time"));
                        });
                // Check, that the changes were applied
                app.given().when().get("/api/filter/this")
                        .then().statusCode(HttpStatus.SC_OK).body(containsString("ok"));
                app.given().when().get("/api/filter/any")
                        .then().statusCode(HttpStatus.SC_NOT_FOUND);
            }
        }
    }
}
