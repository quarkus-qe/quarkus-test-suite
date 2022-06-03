package io.quarkus.ts.http.restclient.reactive.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.MultipartForm;
import org.jboss.resteasy.reactive.RestResponse;

import io.quarkus.logging.Log;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;

@Path("/file")
public class FileResource {
    private static final String BIGGER_THAN_TWO_GIGABYTES = OsUtils.SIZE_2049MiB;
    private final File file;
    private final OsUtils utils;
    private final List<File> deathRow = new LinkedList<>();

    public FileResource(@ConfigProperty(name = "client.filepath") Optional<String> folder) {
        utils = OsUtils.get();
        file = folder
                .stream()
                .map(existing -> java.nio.file.Path.of(existing).resolve("server.txt").toAbsolutePath())
                .peek(path -> {
                    utils.createFile(path.toString(), BIGGER_THAN_TWO_GIGABYTES);
                })
                .map(java.nio.file.Path::toFile)
                .findFirst().orElse(null);
    }

    @GET
    @Path("/download")
    public Uni<File> download() {
        return Uni.createFrom().item(file);
    }

    @POST
    @Path("/upload")
    public Uni<String> upload(File body) {
        deathRow.add(body);
        return utils.getSum(body.getAbsolutePath());
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/upload-multipart")
    @Blocking
    public Uni<String> uploadMultipart(@MultipartForm FileWrapper body) {
        deathRow.add(body.file);
        return utils.getSum(body.file.getAbsolutePath());
    }

    @GET
    @Produces(MediaType.MULTIPART_FORM_DATA)
    @Path("/download-multipart")
    @Blocking //https://github.com/quarkusio/quarkus/issues/25909
    public Uni<FileWrapper> downloadMultipart() {
        FileWrapper wrapper = new FileWrapper();
        wrapper.file = file;
        wrapper.name = file.getName();
        return Uni.createFrom().item(() -> wrapper);
    }

    @GET
    @Produces(MediaType.MULTIPART_FORM_DATA)
    @Path("/download-broken-multipart")
    @Blocking //https://github.com/quarkusio/quarkus/issues/25909
    public Uni<RestResponse> brokenMultipart() {
        return Uni.createFrom().item(() -> RestResponse.ok("Not a multipart message"));
    }

    @GET
    @Path("/hash")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> getHashSum() {
        Log.info("Hashing path " + file.getAbsolutePath());
        return utils.getSum(file.getAbsolutePath());
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
