package io.quarkus.ts.http.restclient.reactive.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.reactive.MultipartForm;

import io.quarkus.logging.Log;
import io.quarkus.ts.http.restclient.reactive.OsUtils;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;

@Path("/file")
public class FileResource {
    private static final String BIGGER_THAN_TWO_GIGABYTES = OsUtils.SIZE_2049MiB;
    private final File FILE = Files.createTempFile("server", ".txt").toAbsolutePath().toFile();
    private final OsUtils utils;

    public FileResource() throws IOException {
        utils = OsUtils.get();
        utils.createFile(FILE.getAbsolutePath(), BIGGER_THAN_TWO_GIGABYTES);
    }

    @GET
    @Path("/download")
    public Uni<Response> download() {
        return Uni.createFrom().item(Response.ok(FILE).build());
    }

    @POST
    @Path("/upload")
    public Uni<Response> upload(File body) {
        return Uni.createFrom().item(Response.ok(utils.getSum(body.getAbsolutePath())).build());
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/upload-multipart")
    @Blocking
    public Uni<String> uploadMultipart(@MultipartForm FileWrapper body) {
        return Uni.createFrom().item(() -> utils.getSum(body.file.getAbsolutePath()));
    }

    @GET
    @Produces(MediaType.MULTIPART_FORM_DATA)
    @Path("/download-multipart")
    public Response downloadMultipart() {
        FileWrapper wrapper = new FileWrapper();
        wrapper.file = FILE;
        return Response.ok(wrapper).build();
    }

    @GET
    @Path("/hash")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<Response> hash() {
        Log.info("Hashing path " + FILE.getAbsolutePath());
        return Uni.createFrom().item(Response.ok(utils.getSum(FILE.getAbsolutePath())).build());
    }
}
