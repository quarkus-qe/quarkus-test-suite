package io.quarkus.ts.configmap.api.server;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperties;

@Path("/hello")
public class HelloResource {

    @ConfigProperties
    HelloConfigProperties configProperties;

    @Path("/message")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMessage(@QueryParam("name") @DefaultValue("World") String name) {
        if (configProperties.message.isPresent()) {
            return Response.ok().entity(new Hello(String.format(configProperties.message.get(), name))).build();
        }

        return Response.serverError().entity("ConfigMap not present").build();
    }

    @Path("/properties")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public HelloConfigProperties getProperties() {
        return configProperties;
    }

}
