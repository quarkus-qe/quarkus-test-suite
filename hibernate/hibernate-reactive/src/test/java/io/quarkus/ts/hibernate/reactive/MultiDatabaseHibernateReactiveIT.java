package io.quarkus.ts.hibernate.reactive;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.MySqlService;
import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.hibernate.reactive.database.Author;
import io.quarkus.ts.hibernate.reactive.database.AuthorIdGenerator;
import io.quarkus.ts.hibernate.reactive.database.AuthorRepository;
import io.quarkus.ts.hibernate.reactive.database.Book;
import io.quarkus.ts.hibernate.reactive.database.ISBNConverter;
import io.quarkus.ts.hibernate.reactive.database.PersonEntity;
import io.quarkus.ts.hibernate.reactive.http.ApplicationExceptionMapper;
import io.quarkus.ts.hibernate.reactive.http.BookDescription;
import io.quarkus.ts.hibernate.reactive.http.GroundedEndpoint;
import io.quarkus.ts.hibernate.reactive.http.OtherResource;
import io.quarkus.ts.hibernate.reactive.http.PanacheEndpoint;
import io.quarkus.ts.hibernate.reactive.http.SomeApi;
import io.quarkus.ts.hibernate.reactive.http.ValidationResource;
import io.quarkus.ts.hibernate.reactive.multidbSources.CarResource;
import io.quarkus.ts.hibernate.reactive.multidbSources.FruitResource;
import io.quarkus.ts.hibernate.reactive.multidbSources.secondDatabase.Fruit;
import io.quarkus.ts.hibernate.reactive.multidbSources.thirdDatabase.Car;
import io.restassured.http.ContentType;

@Tag("QUARKUS-6259")
@QuarkusScenario
public class MultiDatabaseHibernateReactiveIT extends AbstractDatabaseHibernateReactiveIT {
    private static final String SQL_USER = "quarkus_test";
    private static final String SQL_PASSWORD = "quarkus_test";
    private static final String SQL_DATABASE = "quarkus_test";
    private static final int POSTGRES_PORT = 5432;
    private static final int MYSQL_PORT = 3306;

    @Container(image = "${postgresql.latest.image}", port = POSTGRES_PORT, expectedLog = "listening on IPv4 address")
    static PostgresqlService postgresql1 = new PostgresqlService()
            .withUser(SQL_USER)
            .withPassword(SQL_PASSWORD)
            .withDatabase(SQL_DATABASE)
            .withProperty("PGDATA", "/tmp/psql");

    @Container(image = "${postgresql.latest.image}", port = POSTGRES_PORT, expectedLog = "listening on IPv4 address")
    static PostgresqlService postgresql2 = new PostgresqlService()
            .withUser(SQL_USER)
            .withPassword(SQL_PASSWORD)
            .withDatabase(SQL_DATABASE)
            .withProperty("PGDATA", "/tmp/psql");

    @Container(image = "${mysql.80.image}", port = MYSQL_PORT, expectedLog = "Only MySQL server logs after this point")
    static MySqlService mysql = new MySqlService()
            .withUser(SQL_USER)
            .withPassword(SQL_PASSWORD)
            .withDatabase(SQL_DATABASE);

    @QuarkusApplication(properties = "multidb.properties", classes = { Fruit.class, FruitResource.class, Car.class,
            CarResource.class,
            // also include default classes
            Author.class, AuthorIdGenerator.class, AuthorRepository.class, Book.class, ISBNConverter.class, PersonEntity.class,
            ApplicationExceptionMapper.class, BookDescription.class, GroundedEndpoint.class, OtherResource.class,
            PanacheEndpoint.class, SomeApi.class, ValidationResource.class
    })
    static RestService app = new RestService()
            .withProperty("main.url", postgresql1::getReactiveUrl)
            .withProperty("fruits.url", postgresql2::getReactiveUrl)
            .withProperty("cars.url", mysql::getReactiveUrl)
            .withProperty("db.username", SQL_USER)
            .withProperty("db.password", SQL_PASSWORD);

    @Override
    protected RestService getApp() {
        return app;
    }

    @Test
    public void getFruits() {
        /*
         * Fruits are stored in different persistence-unit that is filled by different import script.
         * Test that pre-created fruits (imported via SQL script) are in the DB.
         * Which verifies that the different PU works.
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
         * Cars are stored in different persistence-unit that is filled by different import script.
         * Test that pre-created cars (imported via SQL script) are in the DB.
         * Which verifies that the different PU works.
         */
        String result = getApp().given().contentType(ContentType.JSON)
                .get("car/getAll")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().body().asString();

        assertTrue(result.contains("Audi"), "Should return car that is pre-created in the database, but was: " + result);
    }
}
