package io.quarkus.ts.helm.minimum;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.apache.http.HttpStatus;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.QuarkusHelmClient;
import io.quarkus.test.bootstrap.inject.OpenShiftClient;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.restassured.RestAssured;

@OpenShiftScenario
@DisabledOnNative(reason = "Helm is only concerned with image name, Native compilation is not relevant")
public class OpenShiftHelmSimpleAppIT {

    private static final Logger LOG = Logger.getLogger(OpenShiftHelmSimpleAppIT.class);

    private static final String PLATFORM_OPENSHIFT = "openshift";
    private static final String CHART_NAME = "my-chart";
    private static final String APP_SERVICE_NAME = "minimum-quarkus-helm";
    private static final int TIMEOUT_MIN = 5;
    private static String APP_URL;

    @Inject
    static QuarkusHelmClient helmClient;

    @Inject
    static OpenShiftClient ocpClient;

    @BeforeAll
    public static void tearUp() {
        installChart(CHART_NAME);
    }

    @AfterAll
    public static void tearDown() {
        helmClient.uninstallChart(CHART_NAME);
    }

    @Test
    public void deployHelmQuarkusApplication() {
        assertThat(getAvailableChartNames().toArray(), hasItemInArray(CHART_NAME));
        RestAssured.given().baseUri(APP_URL).get("/greeting")
                .then().statusCode(HttpStatus.SC_OK)
                .body(is("Hello Helm!"));
    }

    @Test
    public void customReadiness() {
        RestAssured.given().baseUri(APP_URL).get("/q/health/ready")
                .then().statusCode(HttpStatus.SC_OK)
                .body("checks[0].name", is("Hello custom Helm Readiness!"));
    }

    private List<String> getAvailableChartNames() {
        List<QuarkusHelmClient.ChartListResult> charts = helmClient.getCharts();
        assertTrue(charts.size() > 0, "Chart " + CHART_NAME + " not found. Installation fail");
        return charts.stream()
                .map(QuarkusHelmClient.ChartListResult::getName)
                .map(String::trim)
                .collect(Collectors.toList());
    }

    private static void installChart(String chartName) {
        String chartFolderName = helmClient.getWorkingDirectory().getAbsolutePath() + "/helm/" + PLATFORM_OPENSHIFT + "/"
                + chartName;
        helmClient.run("dependency", "update", chartFolderName);
        QuarkusHelmClient.Result chartResultCmd = helmClient.installChart(chartName, chartFolderName);
        thenSucceed(chartResultCmd);

        APP_URL = ocpClient.url(APP_SERVICE_NAME).getRestAssuredStyleUri();
        LOG.info("Endpoint URL: " + APP_URL);

        await().ignoreExceptions().atMost(TIMEOUT_MIN, TimeUnit.MINUTES)
                .untilAsserted(() -> RestAssured.given().baseUri(APP_URL).get("/q/health/live")
                        .then().statusCode(HttpStatus.SC_OK));
    }

    private static void thenSucceed(QuarkusHelmClient.Result chartResultCmd) {
        assertTrue(
                chartResultCmd.isSuccessful(),
                String.format("Command %s fails", chartResultCmd.getCommandExecuted()));
    }
}
