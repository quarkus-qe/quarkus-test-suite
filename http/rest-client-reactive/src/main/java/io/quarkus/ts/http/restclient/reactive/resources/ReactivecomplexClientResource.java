package io.quarkus.ts.http.restclient.reactive.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path("/clients/{rootPath}/resource-server")
public class ReactivecomplexClientResource {
    @Path("/{id}")
    @GET
    public String retrieveById(@PathParam("rootPath") String rootPath, @PathParam("id") String id) {
        return "/clients/" + rootPath + "/resource-server/" + id;
    }
}
