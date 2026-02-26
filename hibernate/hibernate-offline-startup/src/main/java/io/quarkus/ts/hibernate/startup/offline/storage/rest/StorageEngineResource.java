package io.quarkus.ts.hibernate.startup.offline.storage.rest;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.hibernate.Session;
import org.hibernate.dialect.Dialect;
import org.hibernate.internal.SessionFactoryImpl;

import io.quarkus.hibernate.orm.PersistenceUnit;

@Path("/storage-engine")
@Produces(MediaType.TEXT_PLAIN)
public class StorageEngineResource {

    @Inject
    @PersistenceUnit("pu1")
    EntityManager emPu1;

    @Inject
    @PersistenceUnit("pu2")
    EntityManager emPu2;

    @GET
    @Path("/pu1/dialect")
    public String getPu1Dialect() {
        Dialect dialect = emPu1.getEntityManagerFactory().unwrap(SessionFactoryImpl.class).getJdbcServices().getDialect();
        return dialect.getTableTypeString();
    }

    @GET
    @Path("/pu1/table-ddl")
    public String getPu1TableDdl() {
        var row = emPu1.unwrap(Session.class)
                .createNativeQuery("SHOW CREATE TABLE Article", Object[].class)
                .getSingleResult();
        return row[1].toString();
    }

    @GET
    @Path("/pu2/dialect")
    public String getPu2Dialect() {
        Dialect dialect = emPu2.getEntityManagerFactory().unwrap(SessionFactoryImpl.class).getJdbcServices().getDialect();
        return dialect.getTableTypeString();
    }

    @GET
    @Path("/pu2/table-ddl")
    public String getPu2TableDdl() {
        var row = emPu2.unwrap(Session.class)
                .createNativeQuery("SHOW CREATE TABLE Article", Object[].class)
                .getSingleResult();
        return row[1].toString();
    }
}