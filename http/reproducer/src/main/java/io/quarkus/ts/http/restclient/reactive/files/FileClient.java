package io.quarkus.ts.http.restclient.reactive.files;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.MultipartForm;

import java.io.File;

@RegisterRestClient
@Path("/file")
@RegisterClientHeaders
public interface FileClient {
    @GET
    @Path("/hash")
    @Produces(MediaType.TEXT_PLAIN)
    String hash();

    @GET
    @Path("/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    Uni<File> download();

    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/upload")
    Uni<String> sendFile(File data);

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/upload-multipart")
    String sendMultipart(@MultipartForm FileWrapper data);

}
