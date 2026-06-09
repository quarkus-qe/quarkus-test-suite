package io.quarkus.qe.hibernate;

import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.qe.hibernate.data.TestDataEntity;
import io.quarkus.test.bootstrap.DevModeQuarkusService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.test.utils.AwaitilityUtils;

@QuarkusScenario
public class DevModeHibernateIT extends BaseHibernateIT {

    private static final String TEST_DATA_ENTITY_PATH = "src/main/java/io/quarkus/qe/hibernate/data/TestDataEntity.java";

    @DevModeQuarkusApplication
    static DevModeQuarkusService app = new DevModeQuarkusService();

    @Test
    void testNamedQueryValidationExceptionForEnum() {
        app.given()
                .queryParam("enums", List.of(1 + "-" + TestDataEntity.Character.OLD,
                        2 + "-" + TestDataEntity.Character.UPDATED,
                        3 + "-" + TestDataEntity.Character.NEW))
                .put("/test-data/upsert-enum-values")
                .then().statusCode(HttpStatus.SC_NO_CONTENT);
        // use named query and find the entity by character
        app.given()
                .queryParam("character", TestDataEntity.Character.OLD)
                .get("/test-data/get-using-named-query-with-enum-constant")
                .then().statusCode(HttpStatus.SC_OK)
                .body(is("DataOne"));
        AtomicReference<String> fileContent = new AtomicReference<>();
        app.modifyFile(TEST_DATA_ENTITY_PATH, currentContent -> {
            fileContent.set(currentContent);
            // this sets @NamedQuery with Character enum constant that doesn't exist
            return currentContent.lines()
                    .map(line -> {
                        if (line.contains("@NamedQuery")) {
                            return "@NamedQuery(name = \"TestDataEntity.FindByCharacter\", query = \"from TestDataEntity"
                                    + " where character = TestDataEntity.Character.UNKNOWN order by id\")";
                        }
                        return line;
                    })
                    .collect(Collectors.joining(System.lineSeparator()));
        });
        AwaitilityUtils.untilAsserted(() -> app.given()
                .queryParam("character", TestDataEntity.Character.UPDATED)
                .get("/test-data/get-using-named-query-with-enum-constant")
                .then().statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR));
        // the named query is just a string, but Hibernate must validate it and recognize that the enum does not exist
        app.logs().assertContains("NamedQueryValidationException");
        app.modifyFile(TEST_DATA_ENTITY_PATH, currentContent -> fileContent.get());
        AwaitilityUtils.untilAsserted(() -> {
            app.given()
                    .queryParam("enums", List.of(1 + "-" + TestDataEntity.Character.OLD,
                            2 + "-" + TestDataEntity.Character.UPDATED,
                            3 + "-" + TestDataEntity.Character.NEW))
                    .put("/test-data/upsert-enum-values")
                    .then().statusCode(HttpStatus.SC_NO_CONTENT);
            app.given()
                    .queryParam("character", TestDataEntity.Character.NEW)
                    .get("/test-data/get-using-named-query-with-enum-constant")
                    .then().statusCode(HttpStatus.SC_OK)
                    .body(is("DataThree"));
        });
    }
}
