package io.quarkus.ts.security.jwt;

import jakarta.annotation.security.DenyAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@DenyAll
@Path("/denied")
public class DeniedResource {
    @GET
    public String denied() {
        return "This should never happen";
    }
}
