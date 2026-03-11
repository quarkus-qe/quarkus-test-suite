package io.quarkus.ts.scheduling.quartz;

import io.quarkus.test.bootstrap.MySqlService;
import io.quarkus.test.services.Container;

public abstract class BaseMySqlQuartzIT {
    static final String MYSQL_PROPERTIES = "mysql.properties";
    static final int MYSQL_PORT = 3306;

    @Container(image = "${mysql.84.image}", port = MYSQL_PORT, expectedLog = "ready for connections.* port: " + MYSQL_PORT)
    static MySqlService database = new MySqlService();
}
