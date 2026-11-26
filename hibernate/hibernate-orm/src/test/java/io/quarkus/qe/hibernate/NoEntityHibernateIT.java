package io.quarkus.qe.hibernate;

import static org.hamcrest.Matchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.qe.hibernate.entityless.EntitylessEndpoint;
import io.quarkus.qe.hibernate.entityless.PersonRepository;
import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnSemeruJdk;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@Tag("QUARKUS-6965")
// TODO: enable when Semeru with condy bug fix is shipped, expected to land in Jan 2027
@DisabledOnSemeruJdk(reason = "OpenJ9 condy bug breaks Quarkus 4 app startup, see https://github.com/eclipse-openj9/openj9/issues/25187")
public class NoEntityHibernateIT {
    static final int POSTGRESQL_PORT = 5432;

    @Container(image = "${postgresql.latest.image}", port = POSTGRESQL_PORT, expectedLog = "listening on IPv4 address")
    static final PostgresqlService database = new PostgresqlService();

    @QuarkusApplication(classes = { EntitylessEndpoint.class, PersonRepository.class }, properties = "entityless.properties")
    public static final RestService app = new RestService()
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);

    @Test
    public void nativeQueryGetTest() {
        app.given()
                .param("name", "Harry")
                .get("/entityless/native-query")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("Wizard"));
    }

    @Test
    public void repositoryGetTest() {
        app.given()
                .param("name", "Sauron")
                .get("/entityless/repository")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("Antagonist"));
    }
}
