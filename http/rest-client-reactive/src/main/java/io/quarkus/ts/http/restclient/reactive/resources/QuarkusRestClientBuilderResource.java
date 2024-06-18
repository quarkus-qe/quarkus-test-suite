package io.quarkus.ts.http.restclient.reactive.resources;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput;

import io.quarkus.logging.Log;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import io.quarkus.ts.http.restclient.reactive.UploadFileService;

@Path("/multipart")
public class QuarkusRestClientBuilderResource implements UploadFileService {
    private static final Logger LOGGER = Logger.getLogger(QuarkusRestClientBuilderResource.class);
    private static final String TEXT = "text";
    private static final String FILE = "file";
    private static final String FILE_NAME_EXAMPLE = "example.txt";
    private final UploadFileService uploadFileService;
    private final URI BASEURI;

    /**
     * multipartPostEncoderMode in which the form data are encoded. Possible values are `HTML5`, `RFC1738` and `RFC3986`.
     */
    public QuarkusRestClientBuilderResource(@ConfigProperty(name = "quarkus.http.port") int httpPort,
            @ConfigProperty(name = "quarkus.rest-client.multipart-post-encoder-mode") String multipartPostEncoderMode) {
        this.BASEURI = URI.create("http://localhost:" + httpPort);
        uploadFileService = QuarkusRestClientBuilder.newBuilder()
                .baseUri(URI.create(String.valueOf(BASEURI)))
                .multipartPostEncoderMode(multipartPostEncoderMode)
                .build(UploadFileService.class);
    }

    @Override
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> uploadBooks(MultipartFormDataInput input) {
        var inputPartText = input.getValues().get(TEXT).stream().findFirst().orElse(null);
        var inputPartFile = input.getValues().get(FILE).stream().findFirst().orElse(null);
        Log.debug("inputPartText (value): {} " + inputPartText.getValue());

        if (inputPartText == null || inputPartFile == null) {
            String missingField = inputPartText == null ? TEXT : FILE;
            Log.warn("Multipart Form Data does not contain value of form field: {} " + missingField);
            return Map.of("error", "Missing field '" + missingField + "'");
        }

        try {
            String text = inputPartText.getValue();
            String fileContent = new String(inputPartFile.getFileItem().getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8);

            return Map.of("message", text, "fileContent", fileContent);
        } catch (IOException e) {
            return Map.of("error", "Error processing file");
        }
    }

}