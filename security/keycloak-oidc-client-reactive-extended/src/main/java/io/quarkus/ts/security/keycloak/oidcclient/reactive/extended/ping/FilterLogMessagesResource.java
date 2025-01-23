package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.ping;

import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.jboss.logging.Logger;

import io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.ping.filters.JWKSResponseFilter;
import io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.ping.filters.TokenResponseFilter;
import io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.ping.filters.UserinfoResponseFilter;

@Path("/filter-messages")
public class FilterLogMessagesResource {

    private static final Logger LOG = Logger.getLogger(FilterLogMessagesResource.class);

    @Path("/token")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getTokenFilterMessages() {
        return TokenResponseFilter.interceptedMessageLogs;
    }

    @Path("/userinfo")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getUserInfoFilterMessages() {
        return UserinfoResponseFilter.interceptedMessageLogs;
    }

    @Path("/jwks")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getJwksFilterMessages() {
        return JWKSResponseFilter.interceptedMessageLogs;
    }
}
