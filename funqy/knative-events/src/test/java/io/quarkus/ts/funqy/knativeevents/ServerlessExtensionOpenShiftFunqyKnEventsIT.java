package io.quarkus.ts.funqy.knativeevents;

import static io.quarkus.test.utils.AwaitilityUtils.untilIsTrue;
import static io.quarkus.ts.funqy.knativeevents.Constants.ENV_VAR_NAME;
import static io.quarkus.ts.funqy.knativeevents.Constants.ENV_VAR_VALUE;
import static io.quarkus.ts.funqy.knativeevents.Constants.PING_RESPONSE_TYPE;
import static io.quarkus.ts.funqy.knativeevents.Constants.PING_TRIGGER;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.knative.eventing.FunqyKnativeEventsService;
import io.quarkus.test.services.knative.eventing.OpenShiftExtensionFunqyKnativeEventsService;
import io.quarkus.test.services.knative.eventing.spi.ForwardResponseDTO;
import io.restassured.common.mapper.TypeRef;

@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/1142")
@Tag("use-quarkus-openshift-extension")
@Tag("serverless")
@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingOpenShiftExtension)
public class ServerlessExtensionOpenShiftFunqyKnEventsIT {

    @QuarkusApplication
    static FunqyKnativeEventsService service = new OpenShiftExtensionFunqyKnativeEventsService()
            .withDefaultBroker()
            .withTrigger().name("unknown").defaultBroker().filterCloudEventType("unknown").endTrigger()
            .withTrigger().name(PING_TRIGGER).defaultBroker().filterCloudEventType(PING_TRIGGER).endTrigger()
            .withTrigger().name("pong").defaultBroker().filterCloudEventType(PING_RESPONSE_TYPE).endTrigger()
            .withTrigger().name("pung").defaultBroker().filterCloudEventType("pong-type").endTrigger()
            .withTrigger().name("peng").defaultBroker().filterCloudEventType("peng").endTrigger()
            .withProperty(ENV_VAR_NAME, ENV_VAR_VALUE);

    @Test
    public void testComplexChain() {
        final Set<ValidationResult> validationResults = new HashSet<>();
        final var expected = new HashSet<>(EnumSet.allOf(ValidationResult.Functions.class));

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

            return validationResults.size() >= expected.size();
        });

        // assert results
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
