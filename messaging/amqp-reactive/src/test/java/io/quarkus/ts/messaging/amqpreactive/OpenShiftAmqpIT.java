package io.quarkus.ts.messaging.amqpreactive;

import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.EnabledIfOpenShiftScenarioPropertyIsTrue;

@OpenShiftScenario
@EnabledIfOpenShiftScenarioPropertyIsTrue
public class OpenShiftAmqpIT extends AmqpIT {
}
