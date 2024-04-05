package io.quarkus.ts.security.jpa;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.OracleService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-3866")
@QuarkusScenario
public class OracleSha512JpaIT extends BaseJpaSecurityRealmIT {

    static final int ORACLE_PORT = 1521;

    @Container(image = "${oracle.image}", port = ORACLE_PORT, expectedLog = "DATABASE IS READY TO USE!")
    static OracleService database = new OracleService();

    @QuarkusApplication(classes = { AdminResource.class, PublicResource.class, UserResource.class,
            SHA512PasswordProvider.class, SHA512UserEntity.class, CreateUserWithSHA512PassResource.class })
    static RestService app = new RestService().withProperties("oracle.properties")
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);
}
