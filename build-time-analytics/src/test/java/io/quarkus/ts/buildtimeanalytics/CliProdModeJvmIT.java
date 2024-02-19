package io.quarkus.ts.buildtimeanalytics;

import static io.quarkus.ts.buildtimeanalytics.AnalyticsFilesUtils.deleteConfigDir;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsFilesUtils.disableAnalyticsByLocalConfig;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsFilesUtils.disableAnalyticsByRemoteConfig;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsFilesUtils.recreateConfigDir;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsFilesUtils.useRefreshableRemoteConfig;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsUtils.EXTENSION_SET_A;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsUtils.EXTENSION_SET_B;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsUtils.QUARKUS_ANALYTICS_DISABLED_PROPERTY;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsUtils.QUARKUS_ANALYTICS_EVENT_PROD;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsUtils.QUARKUS_ANALYTICS_FAKE_URI_BASE;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsUtils.QUARKUS_ANALYTICS_TIMEOUT_PROPERTY;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsUtils.QUARKUS_ANALYTICS_URI_BASE_PROPERTY;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.QuarkusCliClient.Result;
import io.quarkus.test.bootstrap.QuarkusCliRestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.scenarios.annotations.EnabledWhenLinuxContainersAvailable;

@EnabledWhenLinuxContainersAvailable
@Tag("QUARKUS-2812")
@Tag("quarkus-cli")
@QuarkusScenario
@DisabledOnNative(reason = "Only for JVM verification")
public class CliProdModeJvmIT extends AbstractAnalyticsIT {
    @BeforeEach
    public void beforeEach() {
        recreateConfigDir();
    }

    @Test
    public void noExtensions() {
        QuarkusCliRestService app = createAppDefault();
        Result buildResult = buildApp(app::buildOnJvm);
        verifyBuildSuccessful(buildResult);
        verifyValidPayloadPresent(app);
    }

    @Test
    public void extensionSetA() {
        QuarkusCliRestService app = createAppWithExtensions(EXTENSION_SET_A);
        Result buildResult = buildApp(app::buildOnJvm);
        verifyBuildSuccessful(buildResult);
        verifyValidPayloadPresent(app);
    }

    @Test
    public void extensionSetB() {
        QuarkusCliRestService app = createAppWithExtensions(EXTENSION_SET_B);
        Result buildResult = buildApp(app::buildOnJvm);
        verifyBuildSuccessful(buildResult);
        verifyValidPayloadPresent(app);
    }

    @Test
    public void disabledByNoConfig() {
        deleteConfigDir();
        QuarkusCliRestService app = createAppDefault();
        Result buildResult = buildApp(app::buildOnJvm);
        verifyBuildSuccessful(buildResult);
        verifyPayloadAbsent(app);
    }

    @Test
    public void disabledByLocalConfig() {
        disableAnalyticsByLocalConfig();
        QuarkusCliRestService app = createAppDefault();
        Result buildResult = buildApp(app::buildOnJvm);
        verifyBuildSuccessful(buildResult);
        verifyPayloadAbsent(app);
    }

    @Test
    public void disabledByRemoteConfig() {
        disableAnalyticsByRemoteConfig();
        QuarkusCliRestService app = createAppDefault();
        Result buildResult = buildApp(app::buildOnJvm);
        verifyBuildSuccessful(buildResult);
        verifyPayloadAbsent(app);
    }

    @Test
    public void disabledByProperty() {
        QuarkusCliRestService app = createAppDefault();
        Result buildResult = buildApp(app::buildOnJvm, formatBuildProperty(QUARKUS_ANALYTICS_DISABLED_PROPERTY, "true"));
        verifyBuildSuccessful(buildResult);
        verifyPayloadAbsent(app);
    }

    @Test
    public void disabledByGroupId() {
        QuarkusCliRestService app = createAppWithDeniedGroupId();
        Result buildResult = buildApp(app::buildOnJvm);
        verifyBuildSuccessful(buildResult);
        verifyPayloadAbsent(app);
    }

    @Test
    public void disabledPropertyRecognized() {
        QuarkusCliRestService app = createAppDefault();
        Result buildResult = buildApp(app::buildOnJvm, formatBuildProperty(QUARKUS_ANALYTICS_DISABLED_PROPERTY, "true"));
        verifyBuildPropertyRecognized(buildResult, QUARKUS_ANALYTICS_DISABLED_PROPERTY);
    }

    @Test
    public void timeoutPropertyRecognized() {
        QuarkusCliRestService app = createAppDefault();
        Result buildResult = buildApp(app::buildOnJvm, formatBuildProperty(QUARKUS_ANALYTICS_TIMEOUT_PROPERTY, "100"));
        verifyBuildPropertyRecognized(buildResult, QUARKUS_ANALYTICS_TIMEOUT_PROPERTY);
    }

    @Test
    public void uriPropertyRecognized() {
        QuarkusCliRestService app = createAppDefault();
        Result buildResult = buildApp(app::buildOnJvm,
                formatBuildProperty(QUARKUS_ANALYTICS_URI_BASE_PROPERTY, QUARKUS_ANALYTICS_FAKE_URI_BASE));
        verifyBuildPropertyRecognized(buildResult, QUARKUS_ANALYTICS_URI_BASE_PROPERTY);
    }

    @Test
    public void remoteConfigRefreshed() {
        useRefreshableRemoteConfig();
        QuarkusCliRestService app = createAppDefault();
        buildApp(app::buildOnJvm);
        verifyRemoteConfigRefreshed();
    }

    @Override
    protected String getEventType() {
        return QUARKUS_ANALYTICS_EVENT_PROD;
    }
}
