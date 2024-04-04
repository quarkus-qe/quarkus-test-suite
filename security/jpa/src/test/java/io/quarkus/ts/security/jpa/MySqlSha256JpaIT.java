package io.quarkus.ts.security.jpa;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.MySqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-3866")
@QuarkusScenario
public class MySqlSha256JpaIT extends BaseJpaSecurityRealmIT {

    private static final int MYSQL_PORT = 3306;

    @Container(image = "${mysql.80.image}", port = MYSQL_PORT, expectedLog = "port: " + MYSQL_PORT)
    static MySqlService database = new MySqlService();

    @QuarkusApplication(classes = { AdminResource.class, PublicResource.class, UserResource.class,
            SHA256PasswordProvider.class, SHA256UserEntity.class, CreateUserWithSHA256PassResource.class })
    static RestService app = new RestService().withProperties("mysql.properties")
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);
}
