package io.quarkus.ts.hibernate.startup.offline.test;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
public class OpenShiftPostgreSqlHibernateOfflineStartupIT extends AbstractHibernateOfflineStartupIT {

    @Container(image = "${postgresql.latest.image}", port = 5432, expectedLog = "listening on IPv4 address")
    static final PostgresqlService db = new PostgresqlService()
            .setAutoStart(false)
            .withProperty("PGDATA", "/tmp/psql");

    @QuarkusApplication(dependencies = @Dependency(artifactId = "quarkus-jdbc-postgresql"))
    static final RestService app = new RestService()
            .withProperty("jdbc-url", "jdbc:postgresql://db:5432/mydb");
}
