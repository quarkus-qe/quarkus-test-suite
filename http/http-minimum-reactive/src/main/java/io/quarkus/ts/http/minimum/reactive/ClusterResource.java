package io.quarkus.ts.http.minimum.reactive;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
