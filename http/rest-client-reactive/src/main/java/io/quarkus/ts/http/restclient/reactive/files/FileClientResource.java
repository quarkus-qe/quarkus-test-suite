package io.quarkus.ts.http.restclient.reactive.files;

import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;

import io.quarkus.logging.Log;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;

@Path("/file-client")
public class FileClientResource {
    private static final long BIGGER_THAN_TWO_GIGABYTES = OsUtils.SIZE_2049MiB;
    private static final long THREE_GIGABYTES = 2178532224L;

    private final java.nio.file.Path file;
    private final java.nio.file.Path largeFile;
    private final List<java.nio.file.Path> deathRow = new LinkedList<>();
    private final FileClient client;
    private final OsUtils utils;

    @Inject
    public FileClientResource(@RestClient FileClient client,
            @ConfigProperty(name = "client.filepath") Optional<String> folder) {
        utils = OsUtils.get();
        file = folder
                .stream()
                .map(existing -> java.nio.file.Path.of(existing).resolve("upload.txt").toAbsolutePath())
                .peek(path -> utils.createFile(path, BIGGER_THAN_TWO_GIGABYTES))
                .findFirst().orElse(null);
        largeFile = folder
                .stream()
                .map(existing -> java.nio.file.Path.of(existing).resolve("upload_big.txt").toAbsolutePath())
                .peek(path -> utils.createFile(path, THREE_GIGABYTES))
                .findFirst().orElse(null);
        this.client = client;
    }

    @GET
    @Path("/client-hash")
    @Blocking
    public Uni<String> calculateHash() {
        return Uni.createFrom().item(() -> utils.getSum(file));
    }

    @GET
    @Path("/client-hash-big")
    @Blocking
    public Uni<String> calculateLargeHash() {
        return Uni.createFrom().item(() -> utils.getSum(largeFile));
    }

    @GET
    @Path("/hash")
    public String hash() {
        return client.hash();
    }

    @GET
    @Path("/download")
    public Uni<String> download() {
        return client.download()
                .map(file -> {
                    java.nio.file.Path path = file.toPath().toAbsolutePath();
                    deathRow.add(path);
                    return path;
                })
                .map(utils::getSum);
    }

    @GET
    @Path("/download-multipart")
    public Uni<String> downloadMultipart() {
        return client.downloadMultipart()
                .map(wrapper -> wrapper.file.toPath())
                .map(java.nio.file.Path::toAbsolutePath)
                .invoke(deathRow::add)
                .map(utils::getSum);
    }

    @GET
    @Path("/download-broken-multipart")
    public Uni<String> downloadMultipartResponse() {
        return client.brokenMultipart()
                .map(wrapper -> wrapper.file.toPath().toAbsolutePath())
                .map(utils::getSum);
    }

    @POST
    @Path("/multipart")
    @Blocking
    public String uploadMultipart() {
        FileWrapper wrapper = new FileWrapper();
        wrapper.file = file.toFile();
        wrapper.name = file.toString();
        return client.sendMultipart(wrapper);
    }

    @POST
    @Path("/upload-file")
    public Uni<String> upload() {
        return client.sendFile(file.toFile());
    }

    @POST
    @Path("/upload-file-big")
    public Uni<String> uploadLarge() {
        return client.sendFile(largeFile.toFile());
    }

    @DELETE
    @Path("/")
    public RestResponse removeTemporaryFiles() {
        for (java.nio.file.Path path : deathRow) {
            try {
                Files.delete(path);
            } catch (IOException e) {
                Log.warn(e);
            }
        }
        return RestResponse.noContent();
    }
}
