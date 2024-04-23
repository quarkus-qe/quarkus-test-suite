package io.quarkus.ts.messaging.kafka.reactive.streams;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario
@DisabledIfSystemProperty(named = "ts.ibm-z-p.missing.services.excludes", matches = "true", disabledReason = "debezium/zookeeper container not available on s390x & ppc64le.")
@DisabledOnRHBQAarch64Native(reason = "https://issues.redhat.com/browse/QUARKUS-4321")
public class OpenShiftStrimziKafkaStreamIT extends StrimziKafkaStreamIT {
}
