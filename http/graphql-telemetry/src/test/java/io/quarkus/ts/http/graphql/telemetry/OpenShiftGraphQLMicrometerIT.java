package io.quarkus.ts.http.graphql.telemetry;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

import io.quarkus.test.scenarios.OpenShiftScenario;

@DisabledForJreRange(max = JRE.JAVA_20, disabledReason = "VTs supported for Java 21+")
@Tag("QUARKUS-6521")
@OpenShiftScenario
public class OpenShiftGraphQLMicrometerIT extends GraphQLMicrometerIT {

}
