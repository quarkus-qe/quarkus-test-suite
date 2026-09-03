package io.quarkus.ts.packaging.treeshake;

import java.util.Set;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.smallrye.jwt.build.Jwt;

/**
 * Issues a signed JWT. Signing exercises SmallRye JWT and jose4j, a non-trivial slice of
 * dependency code, so it verifies tree-shaking keeps the classes the signing path needs.
 */
@Path("/token")
public class TokenResource {

    @GET
    @PermitAll
    @Produces(MediaType.TEXT_PLAIN)
    public String token() {
        return Jwt.issuer("https://my.auth.server/")
                .upn("tester@quarkus.io")
                .groups(Set.of("tester"))
                .sign();
    }
}
