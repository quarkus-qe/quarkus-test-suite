package io.quarkus.ts.configmap.api.server.secrets;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.smallrye.config.SecretKeys;

public abstract class SecretResource {
    public abstract String getProperty(String key);

    @GET
    @Path("/locked/{key}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getSecret(@PathParam("key") String key) {
        Response.ResponseBuilder result;
        try {
            result = Response.ok(getProperty(key));
        } catch (SecurityException ex) {
            result = Response.serverError().entity(ex.getMessage());
        }
        return result.build();
    }

    @Path("/unlocked/{key}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getUnlocked(@PathParam("key") String key) {
        return SecretKeys.doUnlocked(() -> getProperty(key));
    }
}
