package io.quarkus.ts.hibernate.startup.offline.pu.ownconnection.orm;

import static io.quarkus.credentials.CredentialsProvider.PASSWORD_PROPERTY_NAME;
import static io.quarkus.credentials.CredentialsProvider.USER_PROPERTY_NAME;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import org.eclipse.microprofile.config.ConfigProvider;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.UnknownUnwrapTypeException;

import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.supplier.AgroalConnectionFactoryConfigurationSupplier;
import io.agroal.api.configuration.supplier.AgroalConnectionPoolConfigurationSupplier;
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import io.agroal.api.security.SimplePassword;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.tenant.TenantConnectionResolver;
import io.vertx.ext.web.RoutingContext;

import javax.sql.DataSource;

@ApplicationScoped
@PersistenceUnitExtension("own_connection_provider")
public class CustomTenantConnectionResolver implements TenantConnectionResolver {

    private final AgroalDataSource dataSource;

    CustomTenantConnectionResolver(@Named("own_connection_provider") AgroalDataSource dataSource,
            RoutingContext routingContext) {
        this.dataSource = createDatasource(dataSource, routingContext);
    }

    @Override
    public ConnectionProvider resolve(String tenantId) {
        return new CustomConnectionProvider();
    }

    private static String getPassword(RoutingContext routingContext) {
        var password = routingContext.request().getHeader("connection-password");
        return password == null ? "" : password;
    }

    private static String getUsername(RoutingContext routingContext) {
        var username = routingContext.request().getHeader("connection-user");
        return username == null ? "" : username;
    }

    private static AgroalDataSource createDatasource(AgroalDataSource dataSource, RoutingContext routingContext) {
        var existingConfig = dataSource.getConfiguration();
        try {
            return AgroalDataSource.from(new AgroalDataSourceConfigurationSupplier()
                    .dataSourceImplementation(existingConfig.dataSourceImplementation())
                    .metricsEnabled(existingConfig.metricsEnabled())
                    .connectionPoolConfiguration(
                            new AgroalConnectionPoolConfigurationSupplier(existingConfig.connectionPoolConfiguration())
                                    .connectionFactoryConfiguration(
                                            connectionFactory -> new AgroalConnectionFactoryConfigurationSupplier(
                                                    connectionFactory.get())
                                                    .credential(new SimplePassword("") {
                                                        @Override
                                                        public Properties asProperties() {
                                                            var properties = new Properties();
                                                            properties.setProperty(USER_PROPERTY_NAME,
                                                                    getUsername(routingContext));
                                                            properties.setProperty(PASSWORD_PROPERTY_NAME,
                                                                    getPassword(routingContext));
                                                            return properties;
                                                        }
                                                    }))));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private final class CustomConnectionProvider implements ConnectionProvider {
        @Override
        public Connection getConnection() throws SQLException {
            var connection = dataSource.getConnection();
            String schema = ConfigProvider.getConfig()
                    .getOptionalValue("fixed-default-schema", String.class)
                    .orElse("own_connection_provider");
            connection.setSchema(schema);
            return connection;
        }

        @Override
        public void closeConnection(Connection connection) throws SQLException {
            connection.close();
        }

        @Override
        public boolean supportsAggressiveRelease() {
            return true;
        }

        @Override
        public boolean isUnwrappableAs(Class<?> unwrapType) {
            return ConnectionProvider.class.equals(unwrapType) ||
                    DataSource.class.isAssignableFrom(unwrapType) ||
                    AgroalDataSource.class.isAssignableFrom(unwrapType);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T unwrap(Class<T> unwrapType) {
            if (ConnectionProvider.class.equals(unwrapType)) {
                return (T) this;
            } else if (DataSource.class.isAssignableFrom(unwrapType)
                    || AgroalDataSource.class.isAssignableFrom(unwrapType)) {
                return (T) dataSource;
            } else {
                throw new UnknownUnwrapTypeException(unwrapType);
            }
        }
    }
}
