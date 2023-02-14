package io.quarkus.ts.http.advanced.reactive;

import static io.quarkus.ts.http.advanced.reactive.MultipartResource.MULTIPART_FORM_PATH;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

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
        var inputPartText = input.getFormDataMap().get(TEXT).stream().findAny().orElse(null);
        if (inputPartText != null) {
            try {
                String fileContent = IOUtils.toString(input.getFormDataPart(FILE, InputStream.class, null),
                        StandardCharsets.UTF_8);
                String text = inputPartText.getBodyAsString();
                return Response.ok(new MultipartFormDataDTO(text, fileContent)).build();
            } catch (IOException e) {
                LOGGER.errorf("Failed to retrieve form field value: %s", e.getMessage());
            }
        } else {
            LOGGER.warnf("Multipart Form Data does not contain value of form field '%s'.", TEXT);
        }
        return Response.status(Status.BAD_REQUEST).build();
    }
}
