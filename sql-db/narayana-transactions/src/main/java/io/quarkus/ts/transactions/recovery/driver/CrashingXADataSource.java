package io.quarkus.ts.transactions.recovery.driver;

import static io.quarkus.datasource.common.runtime.DataSourceUtil.DEFAULT_DATASOURCE_NAME;
import static io.quarkus.datasource.common.runtime.DatabaseKind.MARIADB;
import static io.quarkus.datasource.common.runtime.DatabaseKind.MSSQL;
import static io.quarkus.datasource.common.runtime.DatabaseKind.MYSQL;
import static io.quarkus.datasource.common.runtime.DatabaseKind.ORACLE;
import static io.quarkus.datasource.common.runtime.DatabaseKind.POSTGRESQL;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.ShardingKeyBuilder;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.mariadb.jdbc.MariaDbDataSource;
import org.postgresql.xa.PGXADataSource;

import com.microsoft.sqlserver.jdbc.SQLServerXADataSource;
import com.mysql.cj.jdbc.MysqlXADataSource;

import io.quarkus.arc.Arc;
import io.quarkus.datasource.runtime.DataSourcesBuildTimeConfig;
import io.quarkus.datasource.runtime.DatabaseKindConverter;
import io.quarkus.runtime.annotations.RegisterForReflection;

import javax.sql.XAConnection;
import javax.sql.XAConnectionBuilder;
import javax.sql.XADataSource;
import oracle.jdbc.xa.client.OracleXADataSource;

// Agroal extension register directly used drivers, but since we are delegating, we need to register it ourselves
@RegisterForReflection(targets = { OracleXADataSource.class, MariaDbDataSource.class, MysqlXADataSource.class,
        SQLServerXADataSource.class })
public final class CrashingXADataSource implements XADataSource {

    /**
     * Prefix of named XA datasource to default driver.
     * Should default XA driver used by Quarkus change, we need to update this map.
     */
    private static final Map<String, Supplier<XADataSource>> dbKindToDelegateSupplier = Map.of(
            POSTGRESQL, PGXADataSource::new,
            ORACLE, () -> {
                try {
                    return new OracleXADataSource();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            },
            MARIADB, MariaDbDataSource::new,
            MSSQL, SQLServerXADataSource::new,
            MYSQL, MysqlXADataSource::new);

    private final XADataSource delegate;
    private String user;
    private String password;
    private String URL;

    public CrashingXADataSource() {

        // find which database we are dealing with
        var dbKind = Arc
                .container()
                .instance(DataSourcesBuildTimeConfig.class)
                .get()
                .getDataSourceRuntimeConfig(DEFAULT_DATASOURCE_NAME) // use default ds in order to determine db kind
                        .dbKind
                .map(dbKindRaw -> new DatabaseKindConverter().convert(dbKindRaw))
                .orElseThrow(); // we require explicitly set db kind for sake of this test

        // create actual XA datasource
        delegate = dbKindToDelegateSupplier.get(dbKind).get();
    }

    @Override
    public XAConnection getXAConnection() throws SQLException {
        return getXAConnection(getUser(), getPassword());
    }

    @Override
    public XAConnection getXAConnection(String user, String password) throws SQLException {
        return new CrashingXAConnection(delegate.getXAConnection(user, password));
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    @Override
    public XAConnectionBuilder createXAConnectionBuilder() throws SQLException {
        return delegate.createXAConnectionBuilder();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return delegate.getParentLogger();
    }

    @Override
    public ShardingKeyBuilder createShardingKeyBuilder() throws SQLException {
        return delegate.createShardingKeyBuilder();
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String url) {
        this.URL = url;
        // we need to use reflection for there is no common interface, but still XA ds implements it
        Arrays
                .stream(this.delegate.getClass().getMethods())
                .filter(method -> "setURL".equalsIgnoreCase(method.getName()))
                .findFirst()
                .ifPresentOrElse(method -> {
                    try {
                        method.invoke(this.delegate, url);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }, () -> {
                    throw new RuntimeException();
                });
    }
}
