package io.quarkus.ts.openshift.messaging.kafka;

import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.EnabledIfOpenShiftScenarioPropertyIsTrue;

@OpenShiftScenario
@EnabledIfOpenShiftScenarioPropertyIsTrue
public class OpenShiftStrimziKafkaStreamIT extends StrimziKafkaStreamIT {
}
