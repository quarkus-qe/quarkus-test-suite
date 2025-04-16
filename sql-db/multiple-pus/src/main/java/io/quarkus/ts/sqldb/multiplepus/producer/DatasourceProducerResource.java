package io.quarkus.ts.sqldb.multiplepus.producer;

import java.sql.SQLException;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.agroal.api.AgroalDataSource;

@Path("/datasource-producer")
public class DatasourceProducerResource {

    @Inject
    AgroalDataSource dataSource;

    @GET
    public String get() throws SQLException {
        return dataSource.getConnection().getMetaData().getDatabaseProductName();
    }
}
