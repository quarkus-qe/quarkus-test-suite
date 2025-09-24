package io.quarkus.ts.sqldb.sqlapp.driver;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

public class TestJdbcDriver implements Driver {

    static {
        try {
            DriverManager.registerDriver(new TestJdbcDriver());
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to register io.quarkus.ts.qe.sqldb.sqlapp.driver.registration.driver.TestJdbcDriver", e);
        }
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        return null;
    }

    @Override
    public boolean acceptsURL(String url) {
        return false;
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
        return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion() {
        return 1;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    @Override
    public Logger getParentLogger() {
        return null;
    }

}
