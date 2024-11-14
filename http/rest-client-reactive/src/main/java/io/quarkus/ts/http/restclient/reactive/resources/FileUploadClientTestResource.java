package io.quarkus.ts.http.restclient.reactive.resources;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.ts.http.restclient.reactive.multipart.FileUploadClient;
import io.quarkus.ts.http.restclient.reactive.multipart.MultipartBodyWithMediaType;
import io.quarkus.ts.http.restclient.reactive.multipart.MultipartBodyWithoutMediaType;

@Path("/client/upload")
public class FileUploadClientTestResource {

    @Inject
    @RestClient
    FileUploadClient fileUploadClient;

    @GET
    @Path("/without-media-type")
    @Produces(MediaType.TEXT_PLAIN)
    public String testUploadWithoutMediaType() {
        MultipartBodyWithoutMediaType data = new MultipartBodyWithoutMediaType();
        data.file = new ByteArrayInputStream("This is sample test content without media type".getBytes(StandardCharsets.UTF_8));
        data.fileName = "fileTest.txt";
        return fileUploadClient.uploadFileWithoutMediaType(data);
    }

    @GET
    @Path("/with-media-type")
    @Produces(MediaType.TEXT_PLAIN)
    public String testUploadWithMediaType() {
        MultipartBodyWithMediaType data = new MultipartBodyWithMediaType();
        data.file = new ByteArrayInputStream("This is sample test content with media type".getBytes(StandardCharsets.UTF_8));
        data.fileName = "fileTest.txt";

        return fileUploadClient.uploadFileWithMediaType(data);
    }
}
