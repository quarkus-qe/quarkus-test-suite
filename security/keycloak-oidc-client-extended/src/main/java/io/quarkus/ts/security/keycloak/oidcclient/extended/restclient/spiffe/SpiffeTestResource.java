package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.spiffe;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.runtime.StartupEvent;

@Path("/spiffe-test")
@ApplicationScoped
public class SpiffeTestResource {

    // This resource ships in the application that every IT in this module starts, but only the SPIFFE tests set
    // this property, so it is genuinely absent elsewhere. It stays Optional because there is no valid non-empty
    // default (an empty-string default is treated as "unset" by SmallRye and would fail startup for a plain String).
    @ConfigProperty(name = "spiffe.test.token-path")
    Optional<String> tokenPath;

    @ConfigProperty(name = "spiffe.test.jwks", defaultValue = "{\"keys\":[]}")
    String jwks;

    void onStartup(@Observes StartupEvent event) {
        if (tokenPath.isPresent() && !tokenPath.get().isEmpty()) {
            try {
                long exp = System.currentTimeMillis() / 1000 - 1;
                String header = Base64.getUrlEncoder().withoutPadding()
                        .encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
                String payload = Base64.getUrlEncoder().withoutPadding()
                        .encodeToString("{\"sub\":\"spiffe://example.org/workload\",\"exp\":%d}".formatted(exp)
                                .getBytes(StandardCharsets.UTF_8));
                Files.writeString(java.nio.file.Path.of(tokenPath.get()), header + "." + payload + ".dummy");
            } catch (IOException e) {
                throw new RuntimeException("Failed to create initial SPIFFE token file", e);
            }
        }
    }

    @POST
    @Path("/token")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response updateToken(String content) {
        if (tokenPath.isEmpty()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("SPIFFE not configured").build();
        }
        try {
            Files.writeString(java.nio.file.Path.of(tokenPath.get()), content);
            return Response.ok().build();
        } catch (IOException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/bundle")
    @Produces(MediaType.APPLICATION_JSON)
    public String getBundle() {
        return jwks;
    }
}
