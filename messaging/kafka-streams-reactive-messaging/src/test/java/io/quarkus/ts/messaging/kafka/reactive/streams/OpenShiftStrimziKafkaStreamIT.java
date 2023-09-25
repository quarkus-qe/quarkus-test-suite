package io.quarkus.ts.messaging.kafka.reactive.streams;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario
@DisabledIfSystemProperty(named = "ts.s390x.missing.services.excludes", matches = "true", disabledReason = "debezium/zookeeper container not available on s390x.")
public class OpenShiftStrimziKafkaStreamIT extends StrimziKafkaStreamIT {
}
