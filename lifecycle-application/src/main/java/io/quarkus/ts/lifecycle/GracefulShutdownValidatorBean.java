package io.quarkus.ts.lifecycle;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class GracefulShutdownValidatorBean {

    private static final Logger LOG = Logger.getLogger(GracefulShutdownValidatorBean.class);
    private static final String TASK_COMPLETED = "GracefulShutdownValidatorTask completed";
    private static final String TASK_INTERRUPTED = "GracefulShutdownValidatorTask interrupted";

    @ConfigProperty(name = "graceful.shutdown.test.enabled", defaultValue = "false")
    boolean gracefulShutdownTestEnabled;

    @Inject
    ManagedExecutor managedExecutor;

    void onStart(@Observes StartupEvent event) {
        if (!gracefulShutdownTestEnabled) {
            LOG.debug("GracefulShutdownValidatorBean disabled (graceful.shutdown.test.enabled=false)");
            return;
        }

        LOG.info("GracefulShutdownValidatorBean enabled - starting shutdown test");

        managedExecutor.execute(() -> {
            try {
                Thread.sleep(5000);
                LOG.info(TASK_COMPLETED);
            } catch (InterruptedException e) {
                LOG.warn(TASK_INTERRUPTED);
                Thread.currentThread().interrupt();
            }
        });
        Quarkus.asyncExit();
    }
}
