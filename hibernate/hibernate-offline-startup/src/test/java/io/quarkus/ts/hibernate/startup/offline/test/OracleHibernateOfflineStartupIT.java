package io.quarkus.ts.hibernate.startup.offline.test;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.bootstrap.OracleService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;

@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/2022")
@QuarkusScenario
public class OracleHibernateOfflineStartupIT extends AbstractHibernateOfflineStartupIT {

    @Container(image = "${oracle.image}", port = 1521, expectedLog = "DATABASE IS READY TO USE!", builder = FixedPortResourceBuilder.class)
    static final OracleService db = new OracleService().withDatabase("APP_SCOPE_DB,REQ_SCOPE_DB,OWN_CONN_DB")
            .setAutoStart(false);

    @QuarkusApplication(dependencies = @Dependency(artifactId = "quarkus-jdbc-oracle"))
    static final RestService app = new RestService()
            .withProperty("jdbc-url", "jdbc:oracle:thin:@localhost:1521")
            .withProperty("fixed-default-schema", db.getUser().toUpperCase())
            .withProperty("quarkus.hibernate-orm.multitenant", "DATABASE")
            .withProperty("quarkus.datasource.app_scope_credentials.jdbc.url", "${jdbc-url}/APP_SCOPE_DB")
            .withProperty("quarkus.datasource.req_scope_credentials.jdbc.url", "${jdbc-url}/REQ_SCOPE_DB")
            .withProperty("quarkus.datasource.own_connection_provider.jdbc.url", "${jdbc-url}/OWN_CONN_DB");

}
