package io.quarkus.ts.spring.web;

public class MariaDBConstants {
    public static final int PORT = 3306;
    public static final String START_LOG_10 = "socket: '/run/mysqld/mysqld.sock'  port: " + PORT;
    public static final String START_LOG_1011 = "Only MySQL server logs after this point";
    public static final String IMAGE_11 = "${mariadb.11.image}";
    public static final String IMAGE_1011 = "${mariadb.1011.image}";
}
