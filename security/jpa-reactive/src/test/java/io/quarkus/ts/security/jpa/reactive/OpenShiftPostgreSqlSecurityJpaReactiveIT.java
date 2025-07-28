package io.quarkus.ts.security.jpa.reactive;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Container;

@OpenShiftScenario
public class OpenShiftPostgreSqlSecurityJpaReactiveIT extends AbstractSecurityJpaReactiveIT {

    @Container(image = "${postgresql.latest.image}", port = 5432, expectedLog = "listening on IPv4 address")
    static final PostgresqlService database = new PostgresqlService()
            .withProperty("PGDATA", "/tmp/psql");
}
