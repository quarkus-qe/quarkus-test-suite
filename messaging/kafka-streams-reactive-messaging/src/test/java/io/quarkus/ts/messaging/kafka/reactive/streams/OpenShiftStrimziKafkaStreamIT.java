package io.quarkus.ts.messaging.kafka.reactive.streams;

import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnAarch64Native;

@OpenShiftScenario
@DisabledOnAarch64Native(reason = "https://issues.redhat.com/browse/QUARKUS-5180")
public class OpenShiftStrimziKafkaStreamIT extends StrimziKafkaStreamIT {
}
