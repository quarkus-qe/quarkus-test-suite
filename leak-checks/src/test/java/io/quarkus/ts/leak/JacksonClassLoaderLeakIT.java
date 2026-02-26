package io.quarkus.ts.leak;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import io.quarkus.ts.leak.metrics.LeakMetrics;
import io.quarkus.ts.leak.metrics.LeakResult;
import io.quarkus.ts.leak.scenarious.LeakScenario;

@QuarkusMainTest
class JacksonClassLoaderLeakIT {

    private static final int LIFECYCLE_RUNS = 200;
    private static final int GC_ATTEMPTS = 3;

    @Test
    void shouldUnloadClassesBetweenQuarkusRuns(QuarkusMainLauncher launcher) {

        System.out.println("Waiting for repeated Quarkus startups to finish...");

        LeakMetrics metrics = new LeakMetrics();
        metrics.record();

        for (int i = 0; i < LIFECYCLE_RUNS; i++) {
            launcher.launch(LeakScenario.JACKSON.name());
        }

        // Request class unloading by calling GC before measuring
        for (int i = 0; i < GC_ATTEMPTS; i++) {
            System.gc();
        }

        LeakResult result = metrics.measure();
        double ratio = result.unloadRatio();

        System.out.println("Loaded delta: " + result.loadedDelta());
        System.out.println("Unloaded delta: " + result.unloadedDelta());
        System.out.printf("Unload ratio: %.2f\n", ratio);

        /*
         * Healthy runs typically unload > 70% of newly loaded classes,
         * while leaking scenarios are around 20%. The 0.6 threshold
         * leaves margin for GC variability while detecting regressions.
         */
        assertThat("Class unloading ratio must be above the threshold",
                ratio, greaterThan(0.6));
    }
}
