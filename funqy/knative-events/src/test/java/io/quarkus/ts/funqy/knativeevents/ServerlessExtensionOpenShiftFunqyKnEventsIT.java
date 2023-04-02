package io.quarkus.ts.funqy.knativeevents;

import static io.quarkus.test.utils.AwaitilityUtils.untilIsTrue;
import static io.quarkus.test.utils.AwaitilityUtils.AwaitilitySettings.usingTimeout;
import static io.quarkus.ts.funqy.knativeevents.Constants.ENV_VAR_NAME;
import static io.quarkus.ts.funqy.knativeevents.Constants.ENV_VAR_VALUE;
import static io.quarkus.ts.funqy.knativeevents.Constants.PING_RESPONSE_TYPE;
import static io.quarkus.ts.funqy.knativeevents.Constants.PING_TRIGGER;
import static java.lang.String.format;
import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Base64;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.fabric8.knative.sources.v1.PingSourceBuilder;
import io.quarkus.test.bootstrap.Action;
import io.quarkus.test.bootstrap.Service;
import io.quarkus.test.bootstrap.inject.OpenShiftClient;
import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.knative.eventing.FunqyKnativeEventsService;
import io.quarkus.test.services.knative.eventing.OpenShiftExtensionFunqyKnativeEventsService;
import io.quarkus.test.services.knative.eventing.spi.ForwardResponseDTO;
import io.restassured.common.mapper.TypeRef;

@Disabled("Disabled as flaky") // TODO mvavrik: investigate why the test is flaky
@Tag("use-quarkus-openshift-extension")
@Tag("serverless")
@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingOpenShiftExtension)
public class ServerlessExtensionOpenShiftFunqyKnEventsIT {

    private static final String IN_AS_BASE_64 = Base64.getEncoder().encodeToString(new byte[] { 0x12, 0x34, 0x56, 0x78 });

    @Inject
    static OpenShiftClient openShiftClient;

    @QuarkusApplication
    static FunqyKnativeEventsService service = new OpenShiftExtensionFunqyKnativeEventsService()
            .onPreStart(new Action() {
                @Override
                public void handle(Service service) {
                    // create PingSource with sink binding to our Knative service
                    // PingSource will send a message that will be received by "fallback" function
                    openShiftClient
                            .getKnClient()
                            .pingSources()
                            .resource(new PingSourceBuilder()
                                    .withNewMetadata().withName("test-ping-source").endMetadata()
                                    .withNewSpec()
                                    .withDataBase64(IN_AS_BASE_64)
                                    .withContentType("application/octet-stream")
                                    .withNewSink()
                                    .withNewRef()
                                    .withApiVersion("v1")
                                    .withKind("Service")
                                    .withName(service.getName())
                                    .endRef()
                                    .endSink()
                                    .endSpec()
                                    .build())
                            .create();
                }
            })
            .withDefaultBroker()
            .withTrigger().name("fallback").defaultBroker().filterCloudEventType("*").endTrigger()
            .withTrigger().name(PING_TRIGGER).defaultBroker().filterCloudEventType(PING_TRIGGER).endTrigger()
            .withTrigger().name("pong").defaultBroker().filterCloudEventType(PING_RESPONSE_TYPE).endTrigger()
            .withTrigger().name("pung").defaultBroker().filterCloudEventType("pong-type").endTrigger()
            .withTrigger().name("peng").defaultBroker().filterCloudEventType("peng").endTrigger()
            .withProperty(ENV_VAR_NAME, ENV_VAR_VALUE);

    @Test
    public void testComplexChain() {
        final Set<ValidationResult> validationResults = new HashSet<>();

        // request validation results
        untilIsTrue(() -> {
            final var response = service
                    .<Set<ValidationResult>> funcInvoker()
                    .appJsonContentType()
                    .cloudEventType("")
                    .asCloudEventObject()
                    .post()
                    .getResponse()
                    .as(new TypeRef<ForwardResponseDTO<Set<ValidationResult>>>() {
                    })
                    .getResponse();

            if (response != null) {
                validationResults.addAll(response);
            }

            return !validationResults.isEmpty();
        },
                // Knative Eventing PingSource triggers every minute (worst case scenario), rest is for chain processing
                usingTimeout(ofSeconds(100)));

        // assert results
        final var expected = new HashSet<>(EnumSet.allOf(ValidationResult.Functions.class));
        validationResults.forEach(result -> {
            // assert no function was invoked more than once
            if (!expected.contains(result.getInvokedFunction())) {
                Assertions.fail(format("Functions %s was invoked more than once.", result.getInvokedFunction()));
            }

            // assert result value
            expected.remove(result.getInvokedFunction());
            assertEquals(result.getInvokedFunction().expectedValue, result.getActualValue(),
                    format("Function '%s' expected value '%s' but received '%s'.", result.getInvokedFunction(),
                            result.getInvokedFunction().expectedValue, result.getActualValue()));
        });

        // assert each function has been invoked
        assertTrue(expected.isEmpty(),
                format("Following functions were not triggered: %s", Arrays.toString(expected.toArray())));
    }

}
