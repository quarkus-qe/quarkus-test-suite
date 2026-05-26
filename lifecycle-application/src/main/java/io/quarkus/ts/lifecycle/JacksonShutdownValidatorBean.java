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
public class JacksonShutdownValidatorBean {

    private static final Logger LOG = Logger.getLogger(JacksonShutdownValidatorBean.class);
    private static final long STARTUP_DELAY_MS = 3000;
    public static final String SHUTDOWN_INITIATED = "JacksonShutdownValidator: shutdown initiated";

    @ConfigProperty(name = "jackson.shutdown.test.enabled", defaultValue = "false")
    boolean jacksonShutdownTestEnabled;

    @Inject
    ManagedExecutor managedExecutor;

    void onStart(@Observes StartupEvent event) {
        if (!jacksonShutdownTestEnabled) {
            LOG.debug("JacksonShutdownValidatorBean disabled (jackson.shutdown.test.enabled=false)");
            return;
        }
        managedExecutor.execute(() -> {
            try {
                Thread.sleep(STARTUP_DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            LOG.info(SHUTDOWN_INITIATED);
            Quarkus.asyncExit();
        });
    }
}
