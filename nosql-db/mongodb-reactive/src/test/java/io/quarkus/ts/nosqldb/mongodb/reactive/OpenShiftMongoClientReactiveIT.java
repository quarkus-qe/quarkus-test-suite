package io.quarkus.ts.nosqldb.mongodb.reactive;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.bootstrap.MongoDbService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
@DisabledIfSystemProperty(named = "ts.ibm-z-p.missing.services.excludes", matches = "true", disabledReason = "bitnami/mongodb container not available on s390x & ppc64le.")
public class OpenShiftMongoClientReactiveIT extends AbstractMongoClientReactiveIT {

    @Container(image = "${mongodb.image}", port = 27017, expectedLog = "Waiting for connections")
    static MongoDbService database = new MongoDbService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.mongodb.connection-string", database::getJdbcUrl);

    @Override
    protected RestService getApp() {
        return app;
    }
}
