package io.quarkus.ts.leak;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
class ClassLoaderLeakIT {

    private static final Logger LOG = Logger.getLogger(ClassLoaderLeakIT.class);

    @Test
    void shouldUnloadClassesBetweenQuarkusRuns(QuarkusMainLauncher launcher) {

        ClassLoadingMXBean bean = ManagementFactory.getClassLoadingMXBean();

        long initialLoaded = bean.getTotalLoadedClassCount();
        long initialUnloaded = bean.getUnloadedClassCount();

        for (int i = 0; i < 200; i++) {
            launcher.launch();
        }

        // Encourage class unloading before measuring
        for (int i = 0; i < 3; i++) {
            System.gc();
        }

        long finalLoaded = bean.getTotalLoadedClassCount();
        long finalUnloaded = bean.getUnloadedClassCount();

        long loadedDelta = finalLoaded - initialLoaded;
        long unloadedDelta = finalUnloaded - initialUnloaded;

        double ratio = (double) unloadedDelta / loadedDelta;

        LOG.info("Loaded delta: " + loadedDelta);
        LOG.info("Unloaded delta: " + unloadedDelta);
        LOG.info("Unload ratio: " + ratio);

        /*
         * In a healthy lifecycle a significant portion of newly loaded
         * classes should be unloaded between runs. Empirical measurements
         * show healthy ratios typically above ~0.5, while leaking scenarios
         * are around ~0.2. The 0.4 threshold provides a safety margin to
         * tolerate GC variability while still detecting regressions.
         */
        assertTrue(ratio > 0.4,
                "Insufficient class unloading detected. Ratio=" + ratio +
                        " (possible classloader leak)");
    }
}
