package io.quarkus.ts.jakarta.data;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.DevModeQuarkusService;
import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.test.utils.AwaitilityUtils;

@QuarkusScenario
public class DevModePostgreSqlJakartaDataIT extends AbstractJakartaDataIT {

    @Container(image = "${postgresql.latest.image}", port = 5432, expectedLog = "listening on IPv4 address")
    static final PostgresqlService database = new PostgresqlService();

    @DevModeQuarkusApplication
    static final DevModeQuarkusService app = createApp(DevModeQuarkusService::new, database, "pg");

    @Order(29)
    @Test
    void testStaticModelChanges() {
        // drop Thursday, which is used in:
        // @Query("WHERE dayOfWeek <> io.quarkus.ts.jakarta.data.db.DayOfWeek.THURSDAY")
        // so expect compilation failure
        app.modifyFile("src/main/java/io/quarkus/ts/jakarta/data/db/DayOfWeek.java", content -> """
                package io.quarkus.ts.jakarta.data.db;

                public enum DayOfWeek {
                    MONDAY,
                    TUESDAY,
                    WEDNESDAY,
                    FRIDAY,
                    SATURDAY,
                    SUNDAY
                }
                """);
        AwaitilityUtils.untilAsserted(() -> app.given()
                .get("/repository/dev-mode/query-with-wrong-enum-constant")
                .then().statusCode(500));
        app.logs().assertContains("org.hibernate.query.SemanticException: "
                + "Could not interpret path expression 'io.quarkus.ts.jakarta.data.db.DayOfWeek.THURSDAY'");
        // put Thursday back and expect recovery
        app.modifyFile("src/main/java/io/quarkus/ts/jakarta/data/db/DayOfWeek.java", content -> """
                package io.quarkus.ts.jakarta.data.db;

                public enum DayOfWeek {
                    MONDAY,
                    TUESDAY,
                    WEDNESDAY,
                    THURSDAY,
                    FRIDAY,
                    SATURDAY,
                    SUNDAY
                }
                """);
        AwaitilityUtils.untilAsserted(() -> app.given()
                .get("/repository/dev-mode/query-with-wrong-enum-constant")
                .then().statusCode(200));
    }
}
