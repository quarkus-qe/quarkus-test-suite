package io.quarkus.ts.hibernate.startup.offline.test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnSemeruJdk;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
// TODO: enable when Semeru with condy bug fix is shipped, expected to land in Jan 2027
@DisabledOnSemeruJdk(reason = "OpenJ9 condy bug breaks Quarkus 4 app startup, see https://github.com/eclipse-openj9/openj9/issues/25187")
public class MariaDBStorageEngineIT extends AbstractStorageEngineIT {

    @QuarkusApplication(dependencies = @Dependency(artifactId = "quarkus-jdbc-mariadb"), properties = "storage-engine-test.properties")
    static final RestService app = new RestService()
            .withProperty("quarkus.datasource.db-kind", "mariadb")
            .withProperty("jdbc-url", "jdbc:mariadb://localhost:3306");
}
