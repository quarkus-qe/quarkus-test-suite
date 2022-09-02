package io.quarkus.ts.http.restclient.reactive.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.smallrye.mutiny.Uni;
import org.jboss.resteasy.reactive.MultipartForm;
import org.jboss.resteasy.reactive.RestResponse;

import io.quarkus.logging.Log;
import io.smallrye.common.annotation.Blocking;

@Path("/file")
public class FileResource {
    private static final long BIGGER_THAN_TWO_GIGABYTES = OsUtils.SIZE_2049MiB;
    private final OsUtils utils;
    private final List<File> deathRow = new LinkedList<>();
    private final File file;

    public FileResource() {
        utils = OsUtils.get();
        java.nio.file.Path path = utils.getTempDirectory().resolve("server.txt").toAbsolutePath();
        file = path.toFile();
        utils.createFile(path, BIGGER_THAN_TWO_GIGABYTES);
    }

    @GET
    @Path("/hash")
    @Produces(MediaType.TEXT_PLAIN)
    public String getHashSum() {
        Log.info("Hashing path " + file.getAbsolutePath());
        return utils.getSum(file.getAbsoluteFile().toPath());
    }

    @GET
    @Path("/download")
    public Uni<File> download() {
        return Uni.createFrom().item(file);
    }

    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/upload")
    public Uni<String> upload(File body) {
        deathRow.add(body);
        return Uni.createFrom().item(() -> utils.getSum(body.getAbsoluteFile().toPath()));
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/upload-multipart")
    @Blocking
    public String uploadMultipart(@MultipartForm FileWrapper body) {
        deathRow.add(body.file);
        return utils.getSum(body.file.getAbsoluteFile().toPath());
    }

    @DELETE
    @Path("/")
    public RestResponse removeTemporaryFiles() {
        for (File path : deathRow) {
            try {
                Files.delete(path.toPath().toAbsolutePath());
            } catch (IOException e) {
                Log.warn(e);
            }
        }
        return RestResponse.noContent();
    }
}
