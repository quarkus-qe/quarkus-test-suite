package io.quarkus.ts.http.restclient.reactive.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.reactive.MultipartForm;
import org.jboss.resteasy.reactive.RestResponse;

import io.quarkus.logging.Log;
import io.smallrye.common.annotation.Blocking;

@Path("/file")
public class FileResource {
    private final OsUtils utils;
    private final List<File> deathRow = new LinkedList<>();

    public FileResource() {
        utils = OsUtils.get();
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
