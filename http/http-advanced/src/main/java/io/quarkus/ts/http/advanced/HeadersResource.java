package io.quarkus.ts.http.advanced;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/headers")
public class HeadersResource {

    @GET
    @Path("/any")
    public String headers() {
        return "ok";
    }

    @GET
    @Path("/no-accept")
    @Produces(MediaType.TEXT_PLAIN)
    public CustomHeaderResponse noAcceptheaders() {
        return new CustomHeaderResponse("ok");
    }

    @GET
    @Path("/pragma")
    public String pragmaHeaderMustBeSet() {
        return "ok";
    }

    @GET
    @Path("/override")
    public Response headersOverride() {
        return Response.ok("ok").header("foo", "abc").build();
    }

}
