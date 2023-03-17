package io.quarkus.ts.http.jakartarest.reactive.client;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/client")
public class MultipartClientResource {

    @Inject
    @RestClient
    MultipartService service;

    @POST
    @Path("/multipart")
    @Produces(MediaType.TEXT_PLAIN)
    public String sendFile() {
        ClientMultipartBody body = new ClientMultipartBody();
        body.pojoData = getData();
        return service.sendMultipartData(body);
    }

    private PojoData getData() {
        PojoData data = new PojoData();
        data.foo = "test1";
        data.bar = 1;
        return data;
    }
}
