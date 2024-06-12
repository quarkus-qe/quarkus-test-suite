package io.quarkus.ts.http.advanced;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import org.jboss.resteasy.annotations.GZIP;

@Path("/gzip")
public class GzipResource {

    @POST
    public String gzip(@GZIP byte[] message) {
        return "OK";
    }

}
