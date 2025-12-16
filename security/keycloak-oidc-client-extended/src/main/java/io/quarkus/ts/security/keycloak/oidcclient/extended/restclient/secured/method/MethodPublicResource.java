package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.secured.method;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/method-public")
public class MethodPublicResource {

    @Inject
    @RestClient
    MethodFilteredClient methodFilteredClient;

    @GET
    @Path("/publicNoAuth")
    public String publicNoAuth() {
        return methodFilteredClient.publicMethodNoAuth();
    }

    @GET
    @Path("/publicAuth")
    public String publicAuth() {
        return methodFilteredClient.publicMethodAuth();
    }

    @GET
    @Path("/securedNoAuth")
    public String securedNoAuth() {
        return methodFilteredClient.securedMethodNoAuth();
    }

    @GET
    @Path("/securedAuth")
    public String securedAuth() {
        return methodFilteredClient.securedMethodAuth();
    }

    @GET
    @Path("/securedNamedAuth")
    public String securedNamedAuth() {
        return methodFilteredClient.securedMethodNamedAuth();
    }
}
