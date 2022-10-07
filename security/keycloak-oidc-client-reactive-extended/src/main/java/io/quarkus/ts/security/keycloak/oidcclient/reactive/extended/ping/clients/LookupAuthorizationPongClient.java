package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.ping.clients;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;

import io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.model.Score;

@RegisterRestClient
@ClientHeaderParam(name = "Authorization", value = "{lookupAuth}")
@Path("/rest-pong")
public interface LookupAuthorizationPongClient {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    String getPong();

    @GET
    @Path("/name/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    String getPongWithPathName(@PathParam("name") String name);

    @POST
    @Path("/withBody")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    String createPongWithBody(Score score);

    @PUT
    @Path("/withBody")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    String updatePongWithBody(Score score);

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    boolean deletePongById(@PathParam("id") String id);

    default String lookupAuth() {
        Config config = ConfigProvider.getConfig();

        String oidcAuthUrl = config.getValue("quarkus.oidc.auth-server-url", String.class);
        String realm = oidcAuthUrl.substring(oidcAuthUrl.lastIndexOf("/") + 1);
        String authUrl = oidcAuthUrl.replace("/realms/" + realm, "");
        String clientId = config.getValue("quarkus.oidc.client-id", String.class);
        String clientSecret = config.getValue("quarkus.oidc.credentials.secret", String.class);

        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(authUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .grantType("password")
                .username("test-user")
                .password("test-user")
                .build();

        String keycloakToken = keycloak.tokenManager().getAccessToken().getToken();
        return "Bearer " + keycloakToken;
    }
}
