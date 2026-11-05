package io.quarkus.ts.http.restclient.reactive.inputstream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

@Path("/input-stream")
public class InputStreamResource {

    public static final String STATIC_HEADER = "x-static-header";

    @POST
    @Path("/count")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.TEXT_PLAIN)
    public long count(InputStream body) throws IOException {
        return body.transferTo(OutputStream.nullOutputStream());
    }

    @POST
    @Path("/count-reactive")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.TEXT_PLAIN)
    public long countReactive(InputStream body) throws IOException {
        return count(body);
    }

    @POST
    @Path("/headers")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.TEXT_PLAIN)
    public String headers(@HeaderParam(STATIC_HEADER) String staticHeader,
            @HeaderParam(HttpHeaders.CONTENT_TYPE) String contentType,
            InputStream body) throws IOException {
        long length = body.transferTo(OutputStream.nullOutputStream());
        return STATIC_HEADER + "=" + staticHeader + ";content-type=" + contentType + ";length=" + length;
    }
}
