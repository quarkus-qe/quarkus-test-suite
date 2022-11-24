package io.quarkus.ts.http.advanced;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/headers")
public class HeadersResource {

    @GET
    @Path("/any")
    public String headers() {
        return "ok";
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
