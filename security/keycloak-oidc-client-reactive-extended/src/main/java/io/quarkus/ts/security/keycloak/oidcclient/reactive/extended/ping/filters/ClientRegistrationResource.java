package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.ping.filters;

import java.util.Set;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.jboss.logging.Logger;

import io.quarkus.oidc.client.registration.ClientMetadata;
import io.quarkus.oidc.client.registration.OidcClientRegistration;
import io.quarkus.oidc.client.registration.RegisteredClient;

@Path("/client-registration")
public class ClientRegistrationResource {

    private static final Logger LOG = Logger.getLogger(ClientRegistrationResource.class);

    @Inject
    OidcClientRegistration clientRegistration;

    @POST
    @Path("/register")
    @Produces(MediaType.APPLICATION_JSON)
    public String registerClient(ClientRegistrationRequest request) {
        try {
            // Use the Builder pattern - cleaner and type-safe
            ClientMetadata clientMetadata = ClientMetadata.builder()
                    .clientName(request.clientName)
                    .redirectUri("http://localhost:8080/callback")
                    .grantTypes(Set.of("authorization_code", "refresh_token"))
                    .build();

            LOG.infof("Registering client with name: %s", request.clientName);

            // Register the client - triggers ClientRegistrationRequestFilter
            RegisteredClient registered = clientRegistration.registerClient(clientMetadata)
                    .await()
                    .indefinitely();

            LOG.infof("Client registered successfully with ID: %s",
                    registered.metadata().getClientId());

            // Return the metadata as JSON string
            return registered.metadata().getMetadataString();

        } catch (Exception e) {
            LOG.errorf(e, "Failed to register client: %s", e.getMessage());
            throw new RuntimeException("Client registration failed: " + e.getMessage(), e);
        }
    }
}
