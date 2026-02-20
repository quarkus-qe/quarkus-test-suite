package io.quarkus.ts.spring.data.primitivetypes.model;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.ts.spring.data.primitivetypes.ProductRepository;

@Path("/product")
public class ProductResource {

    @Inject
    ProductRepository productRepository;

    @GET
    @Path("/find-by-status/{status}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Product> findByStatus(@PathParam("status") String status) {
        return productRepository.findByStatus(status);
    }
}
