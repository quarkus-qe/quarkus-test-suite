package io.quarkus.ts.buildtimeanalytics;

import static io.quarkus.test.utils.AwaitilityUtils.untilAsserted;
import static io.quarkus.test.utils.AwaitilityUtils.AwaitilitySettings.usingTimeout;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsFilesUtils.deleteConfigDir;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsFilesUtils.disableAnalyticsByLocalConfig;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsFilesUtils.recreateConfigDir;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsFilesUtils.useRefreshableRemoteConfig;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsUtils.ANALYTICS_ACTIVATION_LINK;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsUtils.ANALYTICS_ACTIVATION_PROMPT;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsUtils.EXTENSION_SET_A;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsUtils.EXTENSION_SET_B;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsUtils.QUARKUS_ANALYTICS_DISABLED_PROPERTY;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsUtils.QUARKUS_ANALYTICS_EVENT_DEV;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsUtils.QUARKUS_ANALYTICS_FAKE_URI_BASE;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsUtils.QUARKUS_ANALYTICS_TIMEOUT_PROPERTY;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsUtils.QUARKUS_ANALYTICS_URI_BASE_PROPERTY;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsUtils.UNRECOGNIZED_PROPERTY_FORMAT;

import java.time.Duration;

import org.awaitility.core.ThrowingRunnable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.QuarkusCliRestService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.scenarios.annotations.EnabledWhenLinuxContainersAvailable;

@EnabledWhenLinuxContainersAvailable
@Tag("QUARKUS-2812")
@Tag("quarkus-cli")
@QuarkusScenario
@DisabledOnNative(reason = "Only for JVM verification")
public class CliDevModeIT extends AbstractAnalyticsIT {
    private static final Duration DEV_MODE_TIME_TO_LOG_READY = Duration.ofSeconds(10);

    @BeforeEach
    public void beforeEach() {
        recreateConfigDir();
    }

    @Test
    public void promptPresentIfConfigAbsent() {
        deleteConfigDir();
        QuarkusCliRestService app = createAppDefault();
        startDevMode(app);
        verifyPromptPresent(app);
    }

    @Test
    public void promptAbsentIfDisabledByProperty() {
        deleteConfigDir();
        QuarkusCliRestService app = createAppDefault();
        startDevMode(app.withProperty(QUARKUS_ANALYTICS_DISABLED_PROPERTY, "true"));
        verifyPromptAbsent(app);
    }

    @Test
    public void promptAbsentIfConfigPresentAndEnabled() {
        QuarkusCliRestService app = createAppDefault();
        startDevMode(app);
        verifyPromptAbsent(app);
    }

    @Test
    public void promptAbsentIfConfigPresentAndDisabled() {
        disableAnalyticsByLocalConfig();
        QuarkusCliRestService app = createAppDefault();
        startDevMode(app);
        verifyPromptAbsent(app);
    }

    @Test
    public void noExtensions() {
        QuarkusCliRestService app = createAppDefault();
        startDevMode(app);
        verifyValidPayloadPresent(app);
    }

    @Test
    public void extensionSetA() {
        QuarkusCliRestService app = createAppWithExtensions(EXTENSION_SET_A);
        // Extension set has gRPC, use new approach with single HTTP server instance
        startDevMode(app.withProperty("quarkus.grpc.server.use-separate-server", "false"));
        verifyValidPayloadPresent(app);
    }

    @Test
    public void extensionSetB() {
        QuarkusCliRestService app = createAppWithExtensions(EXTENSION_SET_B);
        startDevMode(app);
        verifyValidPayloadPresent(app);
    }

    @Test
    public void disabledPropertyRecognized() {
        QuarkusCliRestService app = createAppDefault();
        startDevMode(app.withProperty(QUARKUS_ANALYTICS_DISABLED_PROPERTY, "true"));
        verifyDevModePropertyRecognized(app, QUARKUS_ANALYTICS_DISABLED_PROPERTY);
    }

    @Test
    public void timeoutPropertyRecognized() {
        QuarkusCliRestService app = createAppDefault();
        startDevMode(app.withProperty(QUARKUS_ANALYTICS_TIMEOUT_PROPERTY, "100"));
        verifyDevModePropertyRecognized(app, QUARKUS_ANALYTICS_TIMEOUT_PROPERTY);
    }

    @Test
    public void uriPropertyRecognized() {
        QuarkusCliRestService app = createAppDefault();
        startDevMode(app.withProperty(QUARKUS_ANALYTICS_URI_BASE_PROPERTY, QUARKUS_ANALYTICS_FAKE_URI_BASE));
        verifyDevModePropertyRecognized(app, QUARKUS_ANALYTICS_URI_BASE_PROPERTY);
    }

    @Test
    public void remoteConfigRefreshed() {
        useRefreshableRemoteConfig();
        QuarkusCliRestService app = createAppDefault();
        startDevMode(app);
        verifyRemoteConfigRefreshed();
    }

    private void startDevMode(RestService app) {
        // Always provide fake analytics URI to avoid sending data.
        app.withProperty(QUARKUS_ANALYTICS_URI_BASE_PROPERTY, QUARKUS_ANALYTICS_FAKE_URI_BASE)
                // save execution time by disabling all dev services
                .withProperty("quarkus.devservices.enabled", "false").start();
    }

    private void verifyPromptPresent(RestService app) {
        waitUntilAsserted(() -> app.logs().assertContains(ANALYTICS_ACTIVATION_LINK, ANALYTICS_ACTIVATION_PROMPT));
    }

    private void verifyPromptAbsent(RestService app) {
        waitUntilAsserted(() -> {
            app.logs().assertDoesNotContain(ANALYTICS_ACTIVATION_LINK);
            app.logs().assertDoesNotContain(ANALYTICS_ACTIVATION_PROMPT);
        });
    }

    protected void verifyDevModePropertyRecognized(RestService app, String property) {
        waitUntilAsserted(() -> app.logs().assertDoesNotContain(String.format(UNRECOGNIZED_PROPERTY_FORMAT, property)));
    }

    private void waitUntilAsserted(ThrowingRunnable assertion) {
        untilAsserted(assertion, usingTimeout(DEV_MODE_TIME_TO_LOG_READY));
    }

    @Override
    protected String getEventType() {
        return QUARKUS_ANALYTICS_EVENT_DEV;
    }
}
