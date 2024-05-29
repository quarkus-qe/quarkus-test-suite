package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.principal;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.principal.clients.JsonTokenClient;

@Path("/json-propagation-filter")
public class JsonTokenResource {

    @Inject
    @RestClient
    JsonTokenClient jsonClient;

    @GET
    public String getUserNameThroughJson() {
        return jsonClient.getUserName();
    }
}
