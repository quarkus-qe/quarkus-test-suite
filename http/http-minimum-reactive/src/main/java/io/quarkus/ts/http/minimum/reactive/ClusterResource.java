package io.quarkus.ts.http.minimum.reactive;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.smallrye.mutiny.Uni;

@Path("/cluster")
public class ClusterResource {
    @GET
    @Path("/default")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Clusters> get() {
        return Uni.createFrom().item(new Clusters(Cluster.createDefault()));
    }
}
