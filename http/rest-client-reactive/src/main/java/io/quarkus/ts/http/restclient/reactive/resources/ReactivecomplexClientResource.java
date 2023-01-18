package io.quarkus.ts.http.restclient.reactive.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/clients/{rootPath}/resource-server")
public class ReactivecomplexClientResource {
    @Path("/{id}")
    @GET
    public String retrieveById(@PathParam("rootPath") String rootPath, @PathParam("id") String id) {
        return "/clients/" + rootPath + "/resource-server/" + id;
    }
}
