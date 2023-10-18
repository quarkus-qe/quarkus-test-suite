package io.quarkus.ts.messaging.kafka.reactive.streams;

import io.quarkus.test.services.quarkus.model.QuarkusProperties;
import io.smallrye.common.os.OS;

public class DisabledOnWindowsWithRhbqCondition {

    public static final String DISABLED_IF_RHBQ_ON_WINDOWS = "io.quarkus.ts.messaging.kafka.reactive.streams."
            + "DisabledOnWindowsWithRhbqCondition#isRunningOnWindowsWithRhbq";

    public static boolean isRunningOnWindowsWithRhbq() {
        return isRunningOnWindows() && isRhbq();
    }

    private static boolean isRunningOnWindows() {
        return OS.current() == OS.WINDOWS;
    }

    private static boolean isRhbq() {
        return QuarkusProperties.getVersion().contains("-redhat-");
    }

}
