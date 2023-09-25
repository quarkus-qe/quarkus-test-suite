package io.quarkus.ts.nosqldb.mongodb;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/1146")
@DisabledIfSystemProperty(named = "ts.s390x.missing.services.excludes", matches = "true", disabledReason = "bitnami/mongodb container not available on s390x.")
public class OpenShiftMongoClientIT extends MongoClientIT {
}
