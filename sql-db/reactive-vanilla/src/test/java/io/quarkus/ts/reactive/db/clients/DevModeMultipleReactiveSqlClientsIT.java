package io.quarkus.ts.reactive.db.clients;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;

import jakarta.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.ts.reactive.db.clients.model.Book;
import io.quarkus.ts.reactive.db.clients.model.HardCoverBook;
import io.quarkus.ts.reactive.db.clients.model.NoteBook;
import io.quarkus.ts.reactive.db.clients.model.SoftCoverBook;
import io.vertx.core.json.JsonObject;

@Tag("fips-incompatible") // TODO: enable when https://github.com/quarkusio/quarkus/issues/40526 gets fixed
@Tag("QUARKUS-1080")
@Tag("QUARKUS-1408")
@QuarkusScenario
public class DevModeMultipleReactiveSqlClientsIT {

    @DevModeQuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.datasource.devservices.init-script-path", "postgresql-init-script.sql")
            .withProperty("quarkus.datasource.mysql.devservices.init-script-path", "mysql-init-script.sql")
            .withProperty("quarkus.datasource.mssql.devservices.init-script-path", "mssql-init-script.sql");

    @Test
    public void verifyReactivePostgresqlRetrieveEntities() {
        given()
                .when().get("/book/postgresql")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("$.size()", greaterThan(2));
    }

    @Test
    public void verifyReactivePostgresqlRetrieveById() {
        given()
                .when().get("/book/postgresql/1")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void verifyReactivePostgresqlCreateEntity() {
        SoftCoverBook softCoverBook = new SoftCoverBook("Sin noticias de Gurb", "Eduardo Mendoza");
        createRecord("/book/postgresql", softCoverBook);
    }

    @Test
    public void verifyReactiveMysqlRetrieveEntities() {
        given()
                .when().get("/book/mysql")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("$.size()", greaterThan(2));
    }

    @Test
    public void verifyReactiveMysqlRetrieveById() {
        given()
                .when().get("/book/mysql/1")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void verifyReactiveMysqlCreateEntity() {
        NoteBook noteBook = new NoteBook("Sin noticias de Gurb", "Eduardo Mendoza");
        createRecord("/book/mysql", noteBook);
    }

    @Test
    public void verifyReactiveMssqlRetrieveEntities() {
        given()
                .when().get("/book/mssql")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("$.size()", greaterThan(2));
    }

    @Test
    public void verifyReactiveMssqlRetrieveById() {
        given()
                .when().get("/book/mssql/1")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void verifyReactiveMssqlCreateEntity() {
        HardCoverBook hardCoverBook = new HardCoverBook("Sin noticias de Gurb", "Eduardo Mendoza");
        createRecord("/book/mssql", hardCoverBook);
    }

    private static void createRecord(String path, Book book) {
        given()
                .body(JsonObject.mapFrom(book).encode())
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .when()
                .post(path)
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .header("Location", not(empty()));
    }
}
