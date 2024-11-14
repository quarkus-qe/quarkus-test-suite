package io.quarkus.ts.http.advanced.reactive;

import static io.quarkus.ts.http.advanced.reactive.MultipartResource.MULTIPART_FORM_PATH;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput;

@Path(MULTIPART_FORM_PATH)
public class MultipartResource {

    private static final Logger LOGGER = Logger.getLogger(MediaTypeResource.class);
    public static final String TEXT = "text";
    public static final String FILE = "file";
    public static final String MULTIPART_FORM_PATH = "/multipart-form-data";

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response multipartFormData(final MultipartFormDataInput input) {
        var inputPartText = input.getValues().get(TEXT).stream().findAny().orElse(null);
        if (inputPartText != null) {
            try {
                var inputPartFile = input.getValues().get(FILE).stream().findAny().orElse(null);
                String fileContent = new String(inputPartFile.getFileItem().getInputStream().readAllBytes(),
                        StandardCharsets.UTF_8);
                String text = inputPartText.getValue();
                return Response.ok(new MultipartFormDataDTO(text, fileContent)).build();
            } catch (IOException e) {
                LOGGER.errorf("Failed to retrieve form field value: %s", e.getMessage());
            }
        } else {
            LOGGER.warnf("Multipart Form Data does not contain value of form field '%s'.", TEXT);
        }
        return Response.status(Status.BAD_REQUEST).build();
    }

    @POST
    @Path("/inputstream/without-media-type")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public String handleInputStreamWithoutMediaType(
            @RestForm("file") InputStream inputStream,
            @RestForm("description") String description) {

        String content;
        try (InputStreamReader imputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(imputStreamReader)) {
            content = bufferedReader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            LOGGER.error("Error reading the uploaded content", e);
            return "Error processing the file.";
        }

        return "Received description: " + description + " with file content: " + content;
    }

    @POST
    @Path("/inputstream/with-media-type")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public String handleInputStreamWithMediaType(
            @RestForm("file") InputStream inputStream,
            @RestForm("description") String description) {

        String content;
        try (InputStreamReader imputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(imputStreamReader)) {
            content = bufferedReader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            LOGGER.error("Error reading the uploaded content", e);
            return "Error processing the uploaded content";
        }
        return "Received description: " + description + " with file content: " + content;
    }
}
