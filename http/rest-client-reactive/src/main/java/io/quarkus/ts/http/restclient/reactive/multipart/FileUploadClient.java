package io.quarkus.ts.http.restclient.reactive.multipart;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
public interface FileUploadClient {

    @POST
    @Path("/upload/without")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    String uploadFileWithoutMediaType(MultipartBodyWithoutMediaType data);

    @POST
    @Path("/upload/with")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    String uploadFileWithMediaType(MultipartBodyWithMediaType data);

}
