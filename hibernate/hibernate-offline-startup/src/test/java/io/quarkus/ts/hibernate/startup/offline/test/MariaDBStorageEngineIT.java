package io.quarkus.ts.hibernate.startup.offline.test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class MariaDBStorageEngineIT extends AbstractStorageEngineIT {

    @QuarkusApplication(dependencies = @Dependency(artifactId = "quarkus-jdbc-mariadb"), properties = "storage-engine-test.properties")
    static final RestService app = new RestService()
            .withProperty("quarkus.datasource.db-kind", "mariadb")
            .withProperty("jdbc-url", "jdbc:mariadb://localhost:3306");
}
