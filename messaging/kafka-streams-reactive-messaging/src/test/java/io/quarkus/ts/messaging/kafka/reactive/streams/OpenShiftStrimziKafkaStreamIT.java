package io.quarkus.ts.messaging.kafka.reactive.streams;

import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario
@DisabledOnRHBQAarch64Native(reason = "https://issues.redhat.com/browse/QUARKUS-4321")
public class OpenShiftStrimziKafkaStreamIT extends StrimziKafkaStreamIT {
}
