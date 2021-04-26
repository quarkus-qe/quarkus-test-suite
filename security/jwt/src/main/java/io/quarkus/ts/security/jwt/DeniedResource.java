package io.quarkus.ts.security.jwt;

import javax.annotation.security.DenyAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@DenyAll
@Path("/denied")
public class DeniedResource {
    @GET
    public String denied() {
        return "This should never happen";
    }
}
