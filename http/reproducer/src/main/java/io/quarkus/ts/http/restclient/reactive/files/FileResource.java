package io.quarkus.ts.http.restclient.reactive.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.MultipartForm;
import org.jboss.resteasy.reactive.RestResponse;

import io.quarkus.logging.Log;
import io.smallrye.common.annotation.Blocking;

@Path("/file")
public class FileResource {
    private static final long BIGGER_THAN_TWO_GIGABYTES = OsUtils.SIZE_2049MiB;
    private final File file;
    private final OsUtils utils;
    private final List<File> deathRow = new LinkedList<>();

    public FileResource(@ConfigProperty(name = "client.filepath") Optional<String> folder) {
        utils = OsUtils.get();
        file = folder
                .stream()
                .map(existing -> java.nio.file.Path.of(existing).resolve("server.txt").toAbsolutePath())
                .peek(path -> utils.createFile(path, BIGGER_THAN_TWO_GIGABYTES))
                .map(java.nio.file.Path::toFile)
                .findFirst().orElse(null);
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
