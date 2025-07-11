package io.quarkus.ts.vertx;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.jboss.logging.Logger;
import org.jboss.logmanager.MDC;

@Liveness
@ApplicationScoped
public class MdcPropagationHealthCheck2 implements HealthCheck {

    private static final Logger LOG = Logger.getLogger(ExternalHealthEndpoint.LOGGER_NAME);

    @Override
    public HealthCheckResponse call() {
        MDC.put(ExternalHealthEndpoint.MDC_KEY, ExternalHealthEndpoint.MDC_VALUE_PREFIX + getClass().getSimpleName());
        Object mdcValue = MDC.get(ExternalHealthEndpoint.MDC_KEY);
        LOG.info("MDC in health check: " + mdcValue);
        LOG.error("Test log with MDC");
        return HealthCheckResponse.up("mdc-propagation-check");
    }
}