package io.quarkus.qe.scheduling.quartz;

import io.quarkus.test.bootstrap.DefaultService;
import io.quarkus.test.services.Container;

public abstract class BaseMySqlQuartzIT {
    static final String MYSQL_PROPERTIES = "mysql.properties";
    static final String MYSQL_USER = "user";
    static final String MYSQL_PASSWORD = "user";
    static final String MYSQL_DATABASE = "mydb";
    static final int MYSQL_PORT = 3306;

    @Container(image = "registry.access.redhat.com/rhscl/mysql-80-rhel7", port = MYSQL_PORT, expectedLog = "Only MySQL server logs after this point")
    static DefaultService database = new DefaultService()
            .withProperty("MYSQL_USER", MYSQL_USER)
            .withProperty("MYSQL_PASSWORD", MYSQL_PASSWORD)
            .withProperty("MYSQL_DATABASE", MYSQL_DATABASE);

    protected static String mysqlJdbcUrl() {
        return database.getHost().replace("http", "jdbc:mysql") + ":" + database.getPort() + "/" + MYSQL_DATABASE;
    }
}
