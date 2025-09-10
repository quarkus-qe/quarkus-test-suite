package io.quarkus.ts.langchain4j;

import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.GitRepositoryQuarkusApplication;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;

@QuarkusScenario
@DisabledOnNative(reason = "https://issues.redhat.com/browse/QUARKUS-6774")
public class MoviesIT {
    private static final String DEFAULT_ARGS = "-DskipTests=true -DskipITs=true -Dquarkus.platform.version=${QUARKUS_PLATFORM_VERSION} -Dquarkus.platform.group-id=${QUARKUS_PLATFORM_GROUP-ID}";
    private static String key;

    static {
        key = ConfigProvider.getConfig().getValue("quarkus.langchain4j.openai.api-key", String.class);
    }

    @Container(image = "${pgvector.image}", port = 5432)
    static PostgresqlService database = new PostgresqlService()
            // store data in /tmp/psql as in OpenShift we don't have permissions to /var/lib/postgresql/data
            .withProperty("PGDATA", "/tmp/psql");

    @GitRepositoryQuarkusApplication(repo = "https://github.com/quarkiverse/quarkus-langchain4j.git", branch = "1.2", contextDir = "samples/movie-similarity-search", mavenArgs = DEFAULT_ARGS
            + " -Dsamples -Dplatform-deps")
    static final RestService app = new RestService()
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl)
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("movies.file", "resource::/movies.csv")
            .withProperty("quarkus.hibernate-orm.schema-management.strategy", "drop-and-create")
            .withProperty("quarkus.langchain4j.openai.api-key", key);

    @Test
    public void recommendations() {
        Response movie = app.given().get("movies/by-title/Shawshank");
        Assertions.assertEquals(200, movie.statusCode());
        long id = movie.jsonPath().getLong("[0].id");
        Assertions.assertEquals(1, id);

        Response recommended = app.given().get("movies/similar/1");
        Assertions.assertEquals(200, recommended.statusCode());
        ResponseBody body = recommended.body();
        Assertions.assertTrue(body.asString().contains("Green Mile"),
                "The recommendations don't contain the expected movie 'Green Mile': " + body.prettyPrint());
    }

}
