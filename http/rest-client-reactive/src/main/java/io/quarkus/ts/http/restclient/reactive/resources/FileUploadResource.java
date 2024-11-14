package io.quarkus.ts.http.restclient.reactive.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.jboss.logging.Logger;

import io.quarkus.ts.http.restclient.reactive.multipart.MultipartBodyWithMediaType;
import io.quarkus.ts.http.restclient.reactive.multipart.MultipartBodyWithoutMediaType;

@Path("/upload")
public class FileUploadResource {

    private static final Logger LOGGER = Logger.getLogger(FileUploadResource.class);

    @POST
    @Path("/without")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public String uploadFileWithoutMediaType(MultipartBodyWithoutMediaType data) {
        String content;
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(data.file, StandardCharsets.UTF_8))) {
            content = bufferedReader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            LOGGER.error("Error reading data file without media type", e);
            return "Error reading data file without media type " + e.getMessage();
        }
        return "File received: " + data.fileName + " content: " + content;
    }

    @POST
    @Path("/with")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public String uploadFileWithMediaType(MultipartBodyWithMediaType data) {
        String content;
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(data.file, StandardCharsets.UTF_8))) {
            content = bufferedReader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            LOGGER.error("Error reading data file with media type", e);
            return "Error reading data file with media type " + e.getMessage();
        }
        return "File received: " + data.fileName + " content: " + content;
    }
}
