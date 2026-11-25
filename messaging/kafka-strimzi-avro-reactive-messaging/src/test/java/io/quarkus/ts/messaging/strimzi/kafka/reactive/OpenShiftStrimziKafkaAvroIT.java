package io.quarkus.ts.messaging.strimzi.kafka.reactive;

import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;

@OpenShiftScenario
@DisabledOnNative(reason = "https://github.com/quarkusio/quarkus/issues/57243") //todo enable when fixed
public class OpenShiftStrimziKafkaAvroIT extends StrimziKafkaAvroIT {
}
