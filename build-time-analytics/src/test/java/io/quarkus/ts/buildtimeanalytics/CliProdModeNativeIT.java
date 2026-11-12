package io.quarkus.ts.buildtimeanalytics;

import static io.quarkus.ts.buildtimeanalytics.utils.AnalyticsFilesUtils.recreateConfigDir;
import static io.quarkus.ts.buildtimeanalytics.utils.AnalyticsUtils.EXTENSION_SET_A;
import static io.quarkus.ts.buildtimeanalytics.utils.AnalyticsUtils.EXTENSION_SET_B;
import static io.quarkus.ts.buildtimeanalytics.utils.AnalyticsUtils.QUARKUS_ANALYTICS_EVENT_PROD;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.QuarkusCliClient.Result;
import io.quarkus.test.bootstrap.QuarkusCliRestService;
import io.quarkus.test.configuration.PropertyLookup;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.EnabledOnNative;
import io.quarkus.test.scenarios.annotations.EnabledWhenLinuxContainersAvailable;

@EnabledWhenLinuxContainersAvailable
@Tag("QUARKUS-2812")
@Tag("quarkus-cli")
@QuarkusScenario
@EnabledOnNative
public class CliProdModeNativeIT extends AbstractAnalyticsIT {

    private static final PropertyLookup NATIVE_IMG_XMX = new PropertyLookup("quarkus.native.native-image-xmx");

    @BeforeEach
    public void beforeEach() {
        recreateConfigDir();
    }

    @Test
    public void extensionSetA() {
        QuarkusCliRestService app = createAppWithExtensions(EXTENSION_SET_A);
        Result buildResult = buildNativeApp(app);
        verifyBuildSuccessful(buildResult);
        verifyValidPayloadPresent(app);
    }

    @Test
    public void extensionSetB() {
        QuarkusCliRestService app = createAppWithExtensions(EXTENSION_SET_B);
        Result buildResult = buildNativeApp(app);
        verifyBuildSuccessful(buildResult);
        verifyValidPayloadPresent(app);
    }

    private Result buildNativeApp(QuarkusCliRestService app) {
        List<String> props = new ArrayList<>();
        final String nativeImgXmx = NATIVE_IMG_XMX.get();
        if (nativeImgXmx != null) {
            props.add(formatBuildProperty(NATIVE_IMG_XMX.getPropertyKey(), nativeImgXmx));
        }
        // TODO remove once Mandrel ships the fix for https://github.com/quarkusio/quarkus/issues/57390
        // see also .github/workflows/daily.yaml (aarch64-linux-build-native-latest)
        if ("aarch64".equals(System.getProperty("os.arch"))) {
            props.add(formatBuildProperty("quarkus.native.additional-build-args", "-H:-AOTSingleCallsiteInline"));
        }
        return buildApp(app::buildOnNative, props.toArray(new String[0]));
    }

    @Override
    protected String getEventType() {
        return QUARKUS_ANALYTICS_EVENT_PROD;
    }
}
