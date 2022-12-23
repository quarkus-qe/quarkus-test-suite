package io.quarkus.ts.http.jaxrs.reactive;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
