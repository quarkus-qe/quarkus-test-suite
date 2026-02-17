package io.quarkus.ts.security.jpa;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.MariaDbService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.security.jpa.md5.CreateUserWithMD5PassResource;
import io.quarkus.ts.security.jpa.md5.MD5PasswordProvider;
import io.quarkus.ts.security.jpa.md5.MD5UserEntity;

@Tag("QUARKUS-3866")
@QuarkusScenario
public class MariaDbMd5JpaIT extends BaseJpaSecurityRealmIT {

    static final int MARIADB_PORT = 3306;

    @Container(image = "${mariadb.11.image}", port = MARIADB_PORT, expectedLog = "socket: '.*sock'  port: " + MARIADB_PORT)
    static MariaDbService database = new MariaDbService();

    @QuarkusApplication(classes = { AdminResource.class, PublicResource.class, UserResource.class,
            MD5PasswordProvider.class, MD5UserEntity.class, CreateUserWithMD5PassResource.class,
            HttpAuthorizationConfiguration.class, HttpAuthenticationConfiguration.class })
    static RestService app = new RestService()
            .withProperties("mariadb_app.properties")
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);
}
