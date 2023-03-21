package io.quarkus.ts.http.jakartarest.reactive;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.stream.Collectors;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.reactive.multipart.FilePart;

@Path("/multipart")
public class MultipartResource {

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.MULTIPART_FORM_DATA)
    public MultipartBody postForm(MultipartBody multipartBody) {
        return multipartBody;
    }

    @POST
    @Path("/text")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public String postFormReturnText(MultipartBody multipartBody) {
        return multipartBody.text;
    }

    @POST
    @Path("/image")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] postFormReturnFile(MultipartBody multipartBody) throws IOException {
        return IOUtils.toByteArray(multipartBody.image.toURI());
    }

    @POST
    @Path("/data")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] postFormReturnData(MultipartBody multipartBody) throws IOException {
        return IOUtils.toByteArray(multipartBody.data.toURI());
    }

    @POST
    @Path("/plain-text-file")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public String postFormReturnPlainTextFile(MultipartBody multipartBody) throws IOException {
        return Files.readString(multipartBody.plainTextFile.toPath());
    }

    @POST
    @Path("/all-file-control-names")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<String> postFormReturnAllFileControlNames(MultipartBody multipartBody) {
        return multipartBody.allFiles.stream().map(FilePart::name).collect(Collectors.toList());
    }

    @POST
    @Path("/echo")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public String echo(String requestBody) {
        return requestBody;
    }
}
