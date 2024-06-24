package io.quarkus.ts.http.restclient.reactive.resources;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.server.multipart.FormValue;
import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput;

import io.quarkus.ts.http.restclient.reactive.multipart.EncoderModeRestClient;
import io.quarkus.ts.http.restclient.reactive.multipart.Html5EncoderModeRestClient;
import io.quarkus.ts.http.restclient.reactive.multipart.Item;
import io.quarkus.ts.http.restclient.reactive.multipart.MyMultipartDTO;
import io.quarkus.ts.http.restclient.reactive.multipart.Rfc1738EncoderModeRestClient;
import io.quarkus.ts.http.restclient.reactive.multipart.Rfc3986EncoderModeRestClient;

@Path("/encode")
public class MultipartPostEncoderModeResource {

    private final Map<String, EncoderModeRestClient> modeToRestClient;

    public MultipartPostEncoderModeResource(@RestClient Html5EncoderModeRestClient html5Client,
            @RestClient Rfc1738EncoderModeRestClient rfc1738Client,
            @RestClient Rfc3986EncoderModeRestClient rfc3986Client) {
        this.modeToRestClient = Map.of("HTML5", html5Client, "RFC1738", rfc1738Client, "RFC3986", rfc3986Client);
    }

    @Path("{encoder-mode}")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public MyMultipartDTO doAPostRequestToSimpleEncodeResource(MultipartFormDataInput input,
            @PathParam("encoder-mode") String encoderMode) {

        EncoderModeRestClient client = modeToRestClient.get(encoderMode);
        if (!modeToRestClient.containsKey(encoderMode)) {
            throw new WebApplicationException("Unsupported encoder mode" + encoderMode, Response.Status.BAD_REQUEST);
        }

        Map<String, Collection<FormValue>> formValues = input.getValues();

        if (!formValues.containsKey("file1") || !formValues.containsKey("file2") || !formValues.containsKey("otherField")) {
            throw new IllegalArgumentException("Missing mandatory fields!");
        }

        java.nio.file.Path file1 = formValues.get("file1").stream().findFirst().get().getFileItem().getFile();
        java.nio.file.Path file2 = formValues.get("file2").stream().findFirst().get().getFileItem().getFile();
        String otherField = formValues.get("otherField").stream().findFirst().get().getValue();

        MyMultipartDTO response = client.doAPostRequestToThisResource(file1, file2, otherField);

        List<Item> items = response.getItems();

        return new MyMultipartDTO(items);
    }

}
