package io.quarkus.ts.hibernate.search;

import io.quarkus.test.bootstrap.DefaultService;
import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class PostgresqlMultitenantHibernateSearchIT extends AbstractMultitenantHibernateSearchIT {

    static final int ELASTIC_PORT = 9200;
    static final int POSTGRESQL_PORT = 5432;

    @Container(image = "${elastic.71.image}", port = ELASTIC_PORT, expectedLog = "started")
    static DefaultService elastic = new DefaultService()
            .withProperty("discovery.type", "single-node")
            // Limit resources as Elasticsearch official docker image use half of available RAM
            .withProperty("ES_JAVA_OPTS", "-Xms1g -Xmx1g");

    @Container(image = "${postgresql.latest.image}", port = POSTGRESQL_PORT, expectedLog = "listening on IPv4 address")
    static PostgresqlService database = new PostgresqlService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperties("postgresql.properties")
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl)
            .withProperty("quarkus.hibernate-search-orm.elasticsearch.hosts",
                    () -> getElasticSearchConnectionChain(elastic.getURI(Protocol.HTTP)));

    @Override
    protected RestService getApp() {
        return app;
    }
}
