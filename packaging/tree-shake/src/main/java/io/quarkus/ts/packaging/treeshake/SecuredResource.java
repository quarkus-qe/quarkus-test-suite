package io.quarkus.ts.packaging.treeshake;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

/**
 * Verifies the JWT (security) and logs through JBoss Logging, whose {@code _$logger}
 * companion classes must survive tree-shaking.
 */
@Path("/secured")
public class SecuredResource {

    private static final Logger LOG = Logger.getLogger(SecuredResource.class);

    @Inject
    JsonWebToken jwt;

    @GET
    @RolesAllowed("tester")
    @Produces(MediaType.TEXT_PLAIN)
    public String secured() {
        LOG.infof("Secured endpoint accessed by %s", jwt.getName());
        return "Hello " + jwt.getName();
    }
}
