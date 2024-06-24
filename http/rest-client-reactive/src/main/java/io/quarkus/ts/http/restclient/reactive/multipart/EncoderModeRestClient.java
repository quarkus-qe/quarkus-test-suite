package io.quarkus.ts.http.restclient.reactive.multipart;

import static jakarta.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;

@Path("/encoder-mode")
public interface EncoderModeRestClient {

    @POST
    @Consumes(MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    MyMultipartDTO doAPostRequestToThisResource(
            @PartType(MediaType.TEXT_PLAIN) @FormParam("file1") java.nio.file.Path file1,
            @PartType(MediaType.TEXT_PLAIN) @FormParam("file2") java.nio.file.Path file2,
            @RestForm String otherField);

}