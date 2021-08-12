package io.quarkus.ts.messaging.kafka.ssl;

import io.quarkus.test.junit.NativeImageTest;
import io.quarkus.test.scenarios.annotations.EnabledOnNative;

@EnabledOnNative
@NativeImageTest
public class NativeSslAlertMonitorIT extends SslAlertMonitorTest {

    private static final String APP_URL = "http://localhost:8081/";

    @Override
    protected String getAppUrl() {
        return APP_URL;
    }
}
