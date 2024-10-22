package io.quarkus.ts.sqldb.sqlapp;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.SqlServerService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.SqlServerContainer;

@DisabledOnNative(reason = "BouncyCastle JSSE FIPS doesn't work in native but is required for FIPS-enabled environments")
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/2017")
@DisabledIfSystemProperty(named = "ts.ibm-z-p.missing.services.excludes", matches = "true", disabledReason = "MSSQL image is not available for IBM s390x and ppc64le")
@QuarkusScenario
public class MssqlDatabaseIT extends AbstractSqlDatabaseIT {

    @SqlServerContainer(tlsEnabled = true)
    static SqlServerService database = new SqlServerService();

    @QuarkusApplication(dependencies = {
            // added here as BouncyCastle JSSE FIPS is not compatible with native mode
            @Dependency(groupId = "io.quarkus", artifactId = "quarkus-security"),
            @Dependency(groupId = "org.bouncycastle", artifactId = "bctls-fips"),
            @Dependency(groupId = "org.bouncycastle", artifactId = "bc-fips")
    })
    static final RestService app = new RestService()
            .withProperties(database::getTlsProperties)
            .withProperties("mssql.properties")
            .withProperty("quarkus.security.security-providers", "BCFIPSJSSE")
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);
}
