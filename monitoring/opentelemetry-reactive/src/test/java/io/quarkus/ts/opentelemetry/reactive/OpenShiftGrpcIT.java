package io.quarkus.ts.opentelemetry.reactive;

import java.util.List;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.GRPCAction;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Probe;
import io.quarkus.test.bootstrap.inject.OpenShiftClient;
import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingOpenShiftExtension)
public class OpenShiftGrpcIT extends GrpcIT {

    @Inject
    static OpenShiftClient oc;

    @Test
    void grpcProbes() {
        List<Pod> pods = oc.podsInService(app);
        Assertions.assertEquals(1, pods.size());
        List<Container> containers = pods.get(0).getSpec().getContainers();
        Assertions.assertEquals(1, containers.size());
        Container container = containers.get(0);
        validateProbe(container.getLivenessProbe());
        validateProbe(container.getReadinessProbe());
    }

    private static void validateProbe(Probe probe) {
        Assertions.assertNotNull(probe);
        GRPCAction grpcAction = probe.getGrpc();
        Assertions.assertNotNull(grpcAction);
        Assertions.assertEquals("grpc.health.v1.HealthService", grpcAction.getService());
        Assertions.assertEquals(9000, grpcAction.getPort());
    }
}
