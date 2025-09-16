package io.quarkus.ts.sqldb.multiplepus.producer;

import java.sql.SQLException;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.agroal.api.AgroalDataSource;
import io.quarkus.arc.InjectableInstance;

@Path("/datasource-producer")
public class DatasourceProducerResource {

    @Inject
    InjectableInstance<AgroalDataSource> dataSource;

    @GET
    public String get() throws SQLException {
        return dataSource.get().getConnection().getMetaData().getDatabaseProductName();
    }
}
