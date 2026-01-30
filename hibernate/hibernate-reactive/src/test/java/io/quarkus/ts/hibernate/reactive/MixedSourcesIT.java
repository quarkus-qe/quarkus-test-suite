package io.quarkus.ts.hibernate.reactive;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.MySqlService;
import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.hibernate.reactive.database.Author;
import io.quarkus.ts.hibernate.reactive.database.AuthorIdGenerator;
import io.quarkus.ts.hibernate.reactive.database.Book;
import io.quarkus.ts.hibernate.reactive.database.ISBNConverter;
import io.quarkus.ts.hibernate.reactive.database.XmlValidatedCustomer;
import io.quarkus.ts.hibernate.reactive.multidbSources.CarJDBCResource;
import io.quarkus.ts.hibernate.reactive.multidbSources.FruitResource;
import io.quarkus.ts.hibernate.reactive.multidbSources.secondDatabase.Fruit;
import io.quarkus.ts.hibernate.reactive.multidbSources.thirdDatabase.Car;
import io.restassured.http.ContentType;

@Tag("QUARKUS-6259")
// TODO: Re-enable when backport will be producitized
@Disabled("Disabled during 3.27.2.CR1 testing release due to missing backport of https://github.com/quarkusio/quarkus/pull/52011. Commented in Jira issue: https://issues.redhat.com/browse/QUARKUS-6876")
@QuarkusScenario
public class MixedSourcesIT {
    private static final String SQL_USER = "quarkus_test";
    private static final String SQL_PASSWORD = "quarkus_test";
    private static final String SQL_DATABASE = "quarkus_test";
    private static final int POSTGRES_PORT = 5432;
    private static final int MYSQL_PORT = 3306;

    @Container(image = "${postgresql.latest.image}", port = POSTGRES_PORT, expectedLog = "listening on IPv4 address")
    static PostgresqlService postgres = new PostgresqlService()
            .withUser(SQL_USER)
            .withPassword(SQL_PASSWORD)
            .withDatabase(SQL_DATABASE)
            .withProperty("PGDATA", "/tmp/psql");

    @Container(image = "${mysql.80.image}", port = MYSQL_PORT, expectedLog = "Only MySQL server logs after this point")
    static MySqlService mysql = new MySqlService()
            .withUser(SQL_USER)
            .withPassword(SQL_PASSWORD)
            .withDatabase(SQL_DATABASE);

    @QuarkusApplication(properties = "mixed.properties", classes = { Fruit.class, FruitResource.class, Car.class,
            CarJDBCResource.class,
            // also include default classes
            Author.class, AuthorIdGenerator.class, Book.class, ISBNConverter.class, XmlValidatedCustomer.class
    }, dependencies = { @Dependency(artifactId = "quarkus-jdbc-mysql") })
    static RestService app = new RestService()
            .withProperty("fruits.url", postgres::getReactiveUrl)
            .withProperty("cars.url", mysql::getJdbcUrl)
            .withProperty("db.username", SQL_USER)
            .withProperty("db.password", SQL_PASSWORD);

    protected RestService getApp() {
        return app;
    }

    @Test
    public void getFruits() {
        /*
         * Fruits are stored in reactive persistence unit that is filled by separate import script.
         * The method checks that pre-created fruits (imported via SQL script) are in the DB
         */
        String result = getApp().given().contentType(ContentType.JSON)
                .get("fruit/getAll")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().body().asString();
        assertTrue(result.contains("Banana"), "Should return fruit that is pre-created in the database, but was: " + result);
    }

    @Test
    public void getCars() {
        /*
         * Cars are stored in non-reactive persistence unit that is filled by another import script.
         * The method checks that pre-created cars (imported via SQL script) are in the DB.
         */
        String result = getApp().given().contentType(ContentType.JSON)
                .get("car/getAll")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().body().asString();

        assertTrue(result.contains("Audi"), "Should return car that is pre-created in the database, but was: " + result);
    }
}
