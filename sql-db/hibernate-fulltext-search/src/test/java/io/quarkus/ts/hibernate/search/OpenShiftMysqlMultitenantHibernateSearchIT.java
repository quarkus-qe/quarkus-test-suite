package io.quarkus.ts.hibernate.search;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.bootstrap.DefaultService;
import io.quarkus.test.bootstrap.MySqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
@DisabledIfSystemProperty(named = "ts.ibm-z-p.missing.services.excludes", matches = "true", disabledReason = "elasticsearch container not available on s390x & ppc64le.")
@EnabledIfSystemProperty(named = "ts.redhat.registry.enabled", matches = "true")
public class OpenShiftMysqlMultitenantHibernateSearchIT extends AbstractMultitenantHibernateSearchIT {
    private static final int ELASTIC_PORT = 9200;
    private static final int MYSQL_PORT = 3306;
    private static final String MAX_ALLOWED_PACKET_VALUE = "5526600";
    private static final String MAX_ALLOWED_PACKET_KEY = "MYSQL_MAX_ALLOWED_PACKET";
    private static final String EXPECTED_LOG = "Only MySQL server logs after this point";

    @Container(image = "${mysql.80.image}", port = MYSQL_PORT, expectedLog = EXPECTED_LOG)
    static MySqlService base = new MySqlService()
            .withDatabase("base")
            .withProperty(MAX_ALLOWED_PACKET_KEY, MAX_ALLOWED_PACKET_VALUE);

    @Container(image = "${mysql.80.image}", port = MYSQL_PORT, expectedLog = EXPECTED_LOG)
    static MySqlService company1 = new MySqlService()
            .withDatabase("company1")
            .withProperty(MAX_ALLOWED_PACKET_KEY, MAX_ALLOWED_PACKET_VALUE);

    @Container(image = "${mysql.80.image}", port = MYSQL_PORT, expectedLog = EXPECTED_LOG)
    static MySqlService company2 = new MySqlService()
            .withDatabase("company2")
            .withProperty(MAX_ALLOWED_PACKET_KEY, MAX_ALLOWED_PACKET_VALUE);

    @Container(image = "${elastic.9x.image}", port = ELASTIC_PORT, expectedLog = "started")
    static DefaultService elastic = new DefaultService()
            .withProperty("discovery.type", "single-node")
            // Limit resources as Elasticsearch official docker image use half of available RAM
            .withProperty("ES_JAVA_OPTS", "-Xms1g -Xmx1g")
            // these properties are suggest by https://quarkus.io/guides/elasticsearch#running-an-elasticsearch-cluster
            // and Quarkus failed to connect to the Elasticsearch container without them
            .withProperty("cluster.routing.allocation.disk.threshold_enabled", "false")
            .withProperty("xpack.security.enabled", "false");

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperties("mysql.properties")
            .withProperty("quarkus.datasource.username", base.getUser())
            .withProperty("quarkus.datasource.password", base.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", base::getJdbcUrl)
            .withProperty("quarkus.datasource.base.username", base.getUser())
            .withProperty("quarkus.datasource.base.password", base.getPassword())
            .withProperty("quarkus.datasource.base.jdbc.url", base::getJdbcUrl)
            .withProperty("quarkus.datasource.company1.username", company1.getUser())
            .withProperty("quarkus.datasource.company1.password", company1.getPassword())
            .withProperty("quarkus.datasource.company1.jdbc.url", company1::getJdbcUrl)
            .withProperty("quarkus.datasource.company2.username", company2.getUser())
            .withProperty("quarkus.datasource.company2.password", company2.getPassword())
            .withProperty("quarkus.datasource.company2.jdbc.url", company2::getJdbcUrl)
            .withProperty("quarkus.hibernate-search-orm.elasticsearch.hosts",
                    () -> getElasticSearchConnectionChain(elastic.getURI()));

}
