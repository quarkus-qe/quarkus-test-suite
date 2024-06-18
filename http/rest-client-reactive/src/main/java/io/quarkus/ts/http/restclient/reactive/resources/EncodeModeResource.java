package io.quarkus.ts.http.restclient.reactive.resources;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput;

import io.quarkus.ts.http.restclient.reactive.UploadFileService;

@Path("/encode")
public class EncodeModeResource implements UploadFileService {

    private static final String TEXT = "text";
    private static final String FILE = "file";
    private static final Logger LOGGER = Logger.getLogger(EncodeModeResource.class);

    @Override
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> uploadBooks(MultipartFormDataInput formDataInput) {

        var inputPartText = formDataInput.getValues().get(TEXT).stream().findAny().orElse(null);

        if (inputPartText != null) {
            try {
                var inputPartFile = formDataInput.getValues().get(FILE).stream().findAny().orElse(null);
                String fileContent = new String(inputPartFile.getFileItem().getInputStream().readAllBytes(),
                        StandardCharsets.UTF_8);
                String text = inputPartText.getValue();
                return Map.of("message", text, "fileContent", fileContent);
            } catch (IOException e) {
                LOGGER.errorf("Failed to retrieve form field value: %s ", e.getMessage());
            }
        } else {
            LOGGER.warnf("Multipart Form Data does not contain value of form field '%s'.", TEXT);
        }
        return Map.of();
    }
}
