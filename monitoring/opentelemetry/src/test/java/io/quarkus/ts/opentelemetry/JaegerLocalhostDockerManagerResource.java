package io.quarkus.ts.opentelemetry;

import io.quarkus.test.bootstrap.LocalhostManagedResource;
import io.quarkus.test.bootstrap.ManagedResource;
import io.quarkus.test.bootstrap.ServiceContext;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.containers.JaegerContainerManagedResourceBuilder;

/**
 * This is analogy to {@link Container#portDockerHostToLocalhost()}.
 * It is required because secured communication between Quarkus OpenTelemetry OTLP exporter
 * and traces collector verifies host. But our Windows host runs 'remote' Docker agent.
 * TODO: we should consider supporting 'portDockerHostToLocalhost' on the JaegerContainer annotation
 */
public class JaegerLocalhostDockerManagerResource extends JaegerContainerManagedResourceBuilder {
    @Override
    public ManagedResource build(ServiceContext context) {
        return new LocalhostManagedResource(super.build(context));
    }
}
