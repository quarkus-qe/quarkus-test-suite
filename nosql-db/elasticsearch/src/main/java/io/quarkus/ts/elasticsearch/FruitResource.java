package io.quarkus.ts.elasticsearch;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestQuery;

@Path("/fruits")
public class FruitResource {

    @Inject
    FruitService fruitService;

    @POST
    public Response index(Fruit fruit) throws IOException {
        if (fruit.id == null) {
            fruit.id = UUID.randomUUID().toString();
        }
        fruitService.index(fruit);
        return Response.created(URI.create("/fruits/" + fruit.id)).build();
    }

    @GET
    @Path("/{id}")
    public Fruit get(String id) throws IOException {
        return fruitService.get(id);
    }

    @DELETE
    @Path("/{id}")
    public void delete(String id) throws IOException {
        fruitService.delete(id);
    }

    @GET
    @Path("/search")
    public List<Fruit> search(@RestQuery String name, @RestQuery String color) throws IOException {
        if (name != null) {
            return fruitService.searchByName(name);
        } else if (color != null) {
            return fruitService.searchByColor(color);
        } else {
            throw new BadRequestException("Should provide name or color query parameter");
        }
    }
}
