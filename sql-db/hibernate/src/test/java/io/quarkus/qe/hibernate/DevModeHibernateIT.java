package io.quarkus.qe.hibernate;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.test.utils.SocketUtils;

@QuarkusScenario
public class DevModeHibernateIT extends BaseHibernateIT {

    private static final String DB_IMAGE = "quay.io/quarkusqeteam/postgres";
    private static final String DB_VERSION = "14.2";

    @DevModeQuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.datasource.db-kind", "postgresql")
            .withProperty("quarkus.datasource.devservices.port", Integer.toString(SocketUtils.findAvailablePort()))
            .withProperty("quarkus.datasource.devservices.image-name", String.format("%s:%s", DB_IMAGE, DB_VERSION));
}
