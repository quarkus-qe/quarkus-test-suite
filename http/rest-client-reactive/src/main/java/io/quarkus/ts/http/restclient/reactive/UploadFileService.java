package io.quarkus.ts.http.restclient.reactive;

import java.util.Map;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput;

@RegisterRestClient
@Path("/files")
public interface UploadFileService {
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, String> uploadBooks(MultipartFormDataInput dataParts);

}
