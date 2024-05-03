package io.quarkus.ts.hibernate.search;

import io.quarkus.test.bootstrap.DefaultService;
import io.quarkus.test.bootstrap.MySqlService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class MysqlMultitenantHibernateSearchIT extends AbstractMultitenantHibernateSearchIT {
    static final int ELASTIC_PORT = 9200;
    static final int MYSQL_PORT = 3306;

    @Container(image = "${mysql.upstream.80.image}", port = MYSQL_PORT, expectedLog = "ready for connections")
    static MySqlService base = new MySqlService().withDatabase("base");

    @Container(image = "${mysql.upstream.80.image}", port = MYSQL_PORT, expectedLog = "ready for connections")
    static MySqlService company1 = new MySqlService().withDatabase("company1");

    @Container(image = "${mysql.upstream.80.image}", port = MYSQL_PORT, expectedLog = "ready for connections")
    static MySqlService company2 = new MySqlService().withDatabase("company2");;

    @Container(image = "${elastic.7x.image}", port = ELASTIC_PORT, expectedLog = "started")
    static DefaultService elastic = new DefaultService()
            .withProperty("discovery.type", "single-node")
            // Limit resources as Elasticsearch official docker image use half of available RAM
            .withProperty("ES_JAVA_OPTS", "-Xms1g -Xmx1g");

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
                    () -> getElasticSearchConnectionChain(elastic.getURI(Protocol.HTTP)));

    @Override
    protected RestService getApp() {
        return app;
    }
}
