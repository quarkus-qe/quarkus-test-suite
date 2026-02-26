package io.quarkus.ts.hibernate.startup.offline.test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class MySqlStorageEngineIT extends AbstractStorageEngineIT {

    @QuarkusApplication(dependencies = @Dependency(artifactId = "quarkus-jdbc-mysql"), properties = "storage-engine-test.properties")
    static final RestService app = new RestService()
            .withProperty("quarkus.datasource.db-kind", "mysql")
            .withProperty("jdbc-url", "jdbc:mysql://localhost:3306");

}