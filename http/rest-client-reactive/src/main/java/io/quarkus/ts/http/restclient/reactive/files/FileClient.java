package io.quarkus.ts.http.restclient.reactive.files;

import java.io.File;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.MultipartForm;

import io.smallrye.mutiny.Uni;

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

    @GET
    @Produces(MediaType.MULTIPART_FORM_DATA)
    @Path("/download-multipart")
    Uni<FileWrapper> downloadMultipart();

    @GET
    @Produces(MediaType.MULTIPART_FORM_DATA)
    @Path("/download-broken-multipart")
    Uni<FileWrapper> brokenMultipart();

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
