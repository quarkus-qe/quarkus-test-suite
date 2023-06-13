package io.quarkus.ts.spring.web.reactive;

public class MariaDBConstants {
    public static final int PORT = 3306;
    public static final String START_LOG = "Only MySQL server logs after this point";
    public static final String IMAGE_103 = "${mariadb.103.image}";
    public static final String START_LOG_10 = "socket: '/run/mysqld/mysqld.sock'  port: " + PORT;
    public static final String IMAGE_10 = "${mariadb.10.image}";
}