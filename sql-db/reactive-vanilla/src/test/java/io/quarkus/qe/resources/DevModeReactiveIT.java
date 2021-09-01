package io.quarkus.qe.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.qe.model.Book;
import io.quarkus.qe.model.NoteBook;
import io.quarkus.qe.model.Record;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.vertx.core.json.JsonObject;

@Tag("QUARKUS-1080")
@QuarkusScenario
public class DevModeReactiveIT {

    @DevModeQuarkusApplication
    static RestService app = new RestService();

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
        Book book = new Book("Sin noticias de Gurb", "Eduardo Mendoza");
        createRecord("/book/postgresql", book);
    }

    @Test
    public void verifyReactiveMysqlRetrieveEntities() {
        given()
                .when().get("/notebook/mysql")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("$.size()", greaterThan(2));
    }

    @Test
    public void verifyReactiveMysqlRetrieveById() {
        given()
                .when().get("/notebook/mysql/1")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void verifyReactiveMysqlCreateEntity() {
        NoteBook noteBook = new NoteBook("Sin noticias de Gurb", "Eduardo Mendoza");
        createRecord("/notebook/mysql", noteBook);
    }

    private static void createRecord(String path, Record record) {
        given()
                .body(JsonObject.mapFrom(record).encode())
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .when()
                .post(path)
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .header("Location", not(empty()));
    }
}
