package io.quarkus.ts.http.restclient.reactive;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;

@Path("/root")
@Consumes("text/plain")
@Produces("text/plain")
@ClientHeaderParam(name = "fromRoot", value = "headerValue")
@ClientHeaderParam(name = "overridable", value = "RootClient")
public class ReactiveClientRootResource {

    @GET
    @Path("{part1}/{part2}/{part3}/{part4}/{part5}")
    public String getUriParts(@PathParam("part1") String part1, @PathParam("part2") String part2,
            @PathParam("part3") String part3, @PathParam("part4") String part4,
            @PathParam("part5") String part5) {
        return String.format("%s/%s/%s/%s/%s", part1, part2, part3, part4, part5);
    }
}
