package io.quarkus.ts.infinispan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.InfinispanService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class TransactionalInfinispanIT {

    @Container(image = "${infinispan.image}", port = 11222)
    static InfinispanService infinispan = new InfinispanService()
            .withUsername("admin")
            .withPassword("password");

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.infinispan-client.hosts",
                    () -> infinispan.getURI().toString())
            .withProperties("infinispan-it.properties");

    @BeforeEach
    public void setup() {
        app.given()
                .delete("/books/clear")
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    public void testTransactionCommit() {
        String response = app.given()
                .post("/books/commit")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().asString();

        assertTrue(response.contains("Committed"), "Transaction should have been committed");

        String cacheSizeStr = app.given()
                .get("/books/count")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .asString();

        int cacheSize = Integer.parseInt(cacheSizeStr);
        assertEquals(3, cacheSize, "Cache should contain 3 books after commit");
    }

    @Test
    public void testTransactionRollback() {
        String response = app.given()
                .post("/books/rollback")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().asString();

        assertTrue(response.contains("Rolled back"), "Transaction should have been rolled back");

        String cacheSizeStr = app.given()
                .get("/books/count")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .asString();

        int cacheSize = Integer.parseInt(cacheSizeStr);
        assertEquals(0, cacheSize, "Cache should be empty after rollback");
    }

    @Test
    public void testCommitAndRollback() {
        app.given()
                .post("/books/commit")
                .then()
                .statusCode(HttpStatus.SC_OK);

        app.given()
                .post("/books/rollback")
                .then()
                .statusCode(HttpStatus.SC_OK);

        String cacheSizeStr = app.given()
                .get("/books/count")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .asString();

        int cacheSize = Integer.parseInt(cacheSizeStr);
        assertEquals(3, cacheSize, "Cache should only contain 3 books (from commit, not rollback)");
    }
}