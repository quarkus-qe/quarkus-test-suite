package io.quarkus.ts.hibernate.startup.offline.test;

import static io.quarkus.test.utils.AwaitilityUtils.untilAsserted;
import static io.quarkus.ts.hibernate.startup.offline.test.AbstractHibernateOfflineStartupIT.ONLINE_STARTUP_FAILED_MESSAGE;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnFipsAndJavaVersion;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.hibernate.startup.offline.test.reactive.ReactiveArticle;
import io.quarkus.ts.hibernate.startup.offline.test.reactive.ReactiveDatasourceCredentialProvider;
import io.quarkus.ts.hibernate.startup.offline.test.reactive.ReactiveResource;

@QuarkusScenario
@DisabledOnFipsAndJavaVersion(max = 24, reason = "QUARKUS-9772")
public class HibernateReactiveOfflineStartupIT {

    @Container(image = "${postgresql.latest.image}", port = 5432, expectedLog = "listening on IPv4 address", builder = FixedPortResourceBuilder.class)
    // SCRAM-SHA-256 (Postgres) uses the password as an HMAC key; FIPS PKCS#11 (NSS) rejects short keys (CKR_KEY_SIZE_RANGE), so the short passwords fail
    // TODO: set the FIPS-safe (long) password in the Postgres DatabaseService of the test framework and drop this workaround
    static final PostgresqlService db = new PostgresqlService().withPassword("quarkus_test_fips_long_password")
            .setAutoStart(false);

    @QuarkusApplication(dependencies = {
            @Dependency(artifactId = "quarkus-reactive-pg-client")
    }, properties = "hibernate-reactive.properties", classes = {
            ReactiveResource.class,
            ReactiveArticle.class,
            ReactiveDatasourceCredentialProvider.class
    })
    static final RestService app = new RestService();

    @Test
    void testOfflineStartup() {
        app.logs().assertDoesNotContain(ONLINE_STARTUP_FAILED_MESSAGE);
        db.start();
        untilAsserted(() -> app.given().post("/reactive/create-table").then().statusCode(204));
        ReactiveArticle article = new ReactiveArticle();
        article.setId(6L);
        article.setName("Test Article");
        app.given().body(article).contentType(JSON).post("/reactive/article").then().statusCode(204);
        app.given().pathParam("id", article.getId()).get("/reactive/article/{id}").then().statusCode(200).body("name",
                is(article.getName()));
    }
}
