package io.quarkus.ts.buildtimeanalytics;

import static io.quarkus.ts.buildtimeanalytics.AnalyticsFilesUtils.recreateConfigDir;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsUtils.EXTENSION_SET_A;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsUtils.EXTENSION_SET_B;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsUtils.QUARKUS_ANALYTICS_EVENT_PROD;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.QuarkusCliClient.Result;
import io.quarkus.test.bootstrap.QuarkusCliRestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.EnabledOnNative;

@Tag("QUARKUS-2812")
@Tag("quarkus-cli")
@QuarkusScenario
@EnabledOnNative
public class ProdModeNativeIT extends AbstractAnalyticsIT {
    @BeforeEach
    public void beforeEach() {
        recreateConfigDir();
    }

    @Test
    public void extensionSetA() {
        QuarkusCliRestService app = createAppWithExtensions(EXTENSION_SET_A);
        Result buildResult = buildApp(app::buildOnNative);
        verifyBuildSuccessful(buildResult);
        verifyValidPayloadPresent(app);
    }

    @Test
    public void extensionSetB() {
        QuarkusCliRestService app = createAppWithExtensions(EXTENSION_SET_B);
        Result buildResult = buildApp(app::buildOnNative);
        verifyBuildSuccessful(buildResult);
        verifyValidPayloadPresent(app);
    }

    @Override
    protected String getEventType() {
        return QUARKUS_ANALYTICS_EVENT_PROD;
    }
}
