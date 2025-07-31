package io.quarkus.qe.hibernate.query;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@Path("/native-query")
public class NativeQueryResource {

    @Named("named")
    @Inject
    EntityManager entityManager;

    @Path("/temporal-types")
    @GET
    public String getCustomerCreatedOnYear(@QueryParam("customerId") int customerId) {
        var result = entityManager
                .createNativeQuery("SELECT EXTRACT(YEAR FROM created_on) FROM customer WHERE id = " + customerId);
        var resultObj = result.getSingleResult();
        return resultObj.toString();
    }

    @Path("/basic-array-mapping")
    @GET
    public String getCustomerLicenses(@QueryParam("customerId") int customerId) {
        var result = entityManager
                .createNativeQuery("SELECT array_to_string(licenses, ' - ') FROM customer WHERE id = " + customerId);
        return (String) result.getSingleResult();
    }
}
