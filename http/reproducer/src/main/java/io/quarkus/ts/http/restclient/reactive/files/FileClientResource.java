package io.quarkus.ts.http.restclient.reactive.files;

import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;

import io.quarkus.logging.Log;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;

@Path("/file-client")
public class FileClientResource {
    private static final long BIGGER_THAN_TWO_GIGABYTES = OsUtils.SIZE_2049MiB;

    private final java.nio.file.Path file;
    private final List<java.nio.file.Path> deathRow = new LinkedList<>();
    private final FileClient client;
    private final OsUtils utils;

    @Inject
    public FileClientResource(@RestClient FileClient client) {
        utils = OsUtils.get();
        file = utils.getTempDirectory().resolve("upload.txt").toAbsolutePath();
        utils.createFile(file, BIGGER_THAN_TWO_GIGABYTES);
        this.client = client;
    }

    @GET
    @Path("/client-hash")
    @Blocking
    public Uni<String> calculateHash() {
        return Uni.createFrom().item(() -> utils.getSum(file));
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
