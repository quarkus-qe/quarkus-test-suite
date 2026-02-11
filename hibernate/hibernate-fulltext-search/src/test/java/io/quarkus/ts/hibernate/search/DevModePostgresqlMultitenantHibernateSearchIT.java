package io.quarkus.ts.hibernate.search;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import jakarta.ws.rs.core.Response;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.DefaultService;
import io.quarkus.test.bootstrap.DevModeQuarkusService;
import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.ts.hibernate.search.vegetable.VegetableResource.NameAndDescription;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

@QuarkusScenario
public class DevModePostgresqlMultitenantHibernateSearchIT extends AbstractMultitenantHibernateSearchIT {

    private static final int ELASTIC_PORT = 9200;
    private static final int POSTGRESQL_PORT = 5432;
    private static final String TENANT_ID = "company1";
    private static final String VEGETABLE_ENTITY_CLASS_PATH = "src/main/java/io/quarkus/ts/hibernate/search/Vegetable.java";

    @Container(image = "${elastic.9x.image}", port = ELASTIC_PORT, expectedLog = "started")
    static DefaultService elastic = new DefaultService()
            .withProperty("discovery.type", "single-node")
            // Limit resources as Elasticsearch official docker image use half of available RAM
            .withProperty("ES_JAVA_OPTS", "-Xms1g -Xmx1g")
            // these properties are suggest by https://quarkus.io/guides/elasticsearch#running-an-elasticsearch-cluster
            // and Quarkus failed to connect to the Elasticsearch container without them
            .withProperty("cluster.routing.allocation.disk.threshold_enabled", "false")
            .withProperty("xpack.security.enabled", "false");

    @Container(image = "${postgresql.latest.image}", port = POSTGRESQL_PORT, expectedLog = "listening on IPv4 address")
    static PostgresqlService database = new PostgresqlService();

    @DevModeQuarkusApplication
    static DevModeQuarkusService app = (DevModeQuarkusService) new DevModeQuarkusService()
            .withProperties("postgresql.properties")
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl)
            .withProperty("quarkus.hibernate-search-orm.elasticsearch.hosts", () -> elastic.getURI().toString());

    @Test
    void testStaticMetamodelClassGenerationInDevMode() {
        String vegetableName = "Onion";

        searchForVegetable(vegetableName, "name")
                .statusCode(200)
                .body("", hasSize(0));
        create(new NameAndDescription(vegetableName, null));

        searchForVegetable(vegetableName, "name")
                .statusCode(200)
                .body("", hasSize(1))
                .body("[0].name", Matchers.is(vegetableName));
        /*
         * TODO: uncomment next lines when https://github.com/quarkusio/quarkus/issues/48975 is fixed
         * app.modifyFile(VEGETABLE_ENTITY_CLASS_PATH, content -> content.replace("} // INSERT HERE", """
         * this.description = description;
         * }
         *
         * @FullTextField(analyzer = "description")
         *
         * @Column(length = 50)
         * public String description;
         * """));
         *
         * var anotherVegetable = new NameAndDescription("Potato", "Edible");
         * AwaitilityUtils.untilAsserted(() -> create(anotherVegetable)
         * .body("description", Matchers.is("Edible")));
         *
         * searchForVegetable("Edible", "description")
         * .statusCode(200)
         * .body("", hasSize(1))
         * .body("[0].name", Matchers.is(anotherVegetable.name()));
         */
    }

    private static ValidatableResponse searchForVegetable(String vegetableName, String fieldName) {
        return app.given()
                .queryParam("terms", vegetableName)
                .queryParam("fieldName", fieldName)
                .pathParam("tenantId", TENANT_ID)
                .get("/{tenantId}/vegetables/search")
                .then();
    }

    private static ValidatableResponse create(NameAndDescription nameAndDescription) {
        return app.given()
                .body(nameAndDescription).contentType(ContentType.JSON)
                .pathParam("tenantId", TENANT_ID)
                .post("/{tenantId}/vegetables")
                .then()
                .statusCode(is(Response.Status.CREATED.getStatusCode()))
                .body("id", Matchers.notNullValue());
    }
}
