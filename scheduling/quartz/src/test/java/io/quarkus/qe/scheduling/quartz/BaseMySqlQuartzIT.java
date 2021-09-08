package io.quarkus.qe.scheduling.quartz;

import io.quarkus.test.bootstrap.MySqlService;
import io.quarkus.test.services.Container;

public abstract class BaseMySqlQuartzIT {
    static final String MYSQL_PROPERTIES = "mysql.properties";
    static final int MYSQL_PORT = 3306;

    @Container(image = "${mysql.80.image}", port = MYSQL_PORT, expectedLog = "Only MySQL server logs after this point")
    static MySqlService database = new MySqlService();
}
