package io.quarkus.ts.messaging.cloudevents.amqpjson;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/1144")
public class OpenShiftJSONCloudEventsOverProdAmqpIT extends JSONCloudEventsOverProdAmqpIT {
}
