package io.quarkus.ts.transactions;

import java.sql.Connection;
import java.sql.SQLException;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.agroal.api.AgroalDataSource;

@Path("connection")
public class ConnectionResource {

    @Named("validation-query-timeout")
    @Inject
    AgroalDataSource dataSource;

    @GET
    @Path("validation-query")
    public int validationQuery() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (var statement = connection.createStatement()) {
                var result = statement.executeQuery("SELECT COUNT(*) FROM account");
                result.next();
                return result.getInt(1);
            } finally {
                connection.close();
            }
        }
    }
}
