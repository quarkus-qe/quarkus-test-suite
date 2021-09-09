package io.quarkus.qe.hibernate;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;

@QuarkusScenario
public class DevModeHibernateIT extends BaseHibernateIT {

    @DevModeQuarkusApplication
    static RestService app = new RestService();
}
