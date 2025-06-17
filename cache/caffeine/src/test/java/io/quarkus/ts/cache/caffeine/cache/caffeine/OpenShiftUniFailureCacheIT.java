package io.quarkus.ts.cache.caffeine.cache.caffeine;

import static io.quarkus.test.utils.AwaitilityUtils.untilIsNotNull;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Tag;

import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.ResourceRequirementsBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.dsl.NonDeletingOperation;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.inject.OpenShiftClient;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
@Tag("QUARKUS-4541")
@Tag("openshift")
public class OpenShiftUniFailureCacheIT extends UniFailureCacheIT {

    @Inject
    static OpenShiftClient client;

    @QuarkusApplication
    static RestService app = new RestService()
            .onPostStart(service -> {
                patchDeploymentWithResourceLimits(service.getName());
            });

    private static void patchDeploymentWithResourceLimits(String deploymentName) {

        Deployment deployment = untilIsNotNull(
                () -> client.getFabric8Client().apps().deployments().withName(deploymentName).get());

        // Create resource requirements
        ResourceRequirements resources = new ResourceRequirementsBuilder()
                .addToLimits("memory", new Quantity("192Mi"))
                .addToRequests("memory", new Quantity("96Mi"))
                .build();

        // Update the deployment
        deployment.getSpec().getTemplate().getSpec().getContainers()
                .forEach(container -> container.setResources(resources));

        // Apply the patch
        client.getFabric8Client().apps().deployments()
                .resource(deployment)
                .unlock()
                .createOr(NonDeletingOperation::patch);
    }
}