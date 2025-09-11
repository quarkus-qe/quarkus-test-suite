package io.quarkus.ts.hibernate.search;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.bootstrap.DefaultService;
import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
@DisabledIfSystemProperty(named = "ts.ibm-z-p.missing.services.excludes", matches = "true", disabledReason = "elasticsearch container not available on s390x & ppc64le.")
@EnabledIfSystemProperty(named = "ts.redhat.registry.enabled", matches = "true")
public class OpenShiftPostgresqlMultitenantHibernateSearchIT extends AbstractMultitenantHibernateSearchIT {
    static final int ELASTIC_PORT = 9200;
    static final int POSTGRESQL_PORT = 5432;

    @Container(image = "${elastic.9x.image}", port = ELASTIC_PORT, expectedLog = "started")
    static DefaultService elastic = new DefaultService()
            .withProperty("discovery.type", "single-node")
            // Limit resources as Elasticsearch official docker image use half of available RAM
            .withProperty("ES_JAVA_OPTS", "-Xms1g -Xmx1g")
            // these properties are suggest by https://quarkus.io/guides/elasticsearch#running-an-elasticsearch-cluster
            // and Quarkus failed to connect to the Elasticsearch container without them
            .withProperty("cluster.routing.allocation.disk.threshold_enabled", "false")
            .withProperty("xpack.security.enabled", "false");

    @Container(image = "${postgresql.13.image}", port = POSTGRESQL_PORT, expectedLog = "listening on IPv4 address")
    static PostgresqlService database = new PostgresqlService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperties("postgresql.properties")
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl)
            .withProperty("quarkus.hibernate-search-orm.elasticsearch.hosts",
                    () -> getElasticSearchConnectionChain(elastic.getURI()));

}
