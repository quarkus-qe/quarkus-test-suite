package io.quarkus.ts.nosqldb.mongodb;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.scenarios.OpenShiftScenario;

@Tag("QUARKUS-6233")
@OpenShiftScenario
public class OpenShiftMongoClientMtlsIT extends MongoClientMtlsIT {
}
