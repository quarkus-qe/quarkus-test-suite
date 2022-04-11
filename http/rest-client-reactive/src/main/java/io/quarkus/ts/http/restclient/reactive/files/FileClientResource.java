package io.quarkus.ts.http.restclient.reactive.files;

import java.io.IOException;
import java.nio.file.Files;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.ts.http.restclient.reactive.OsUtils;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;

@Path("/file-client")
public class FileClientResource {
    private static final String BIGGER_THAN_TWO_GIGABYTES = OsUtils.SIZE_2049MiB;
    private final java.nio.file.Path FILE = Files.createTempFile("upload", "txt").toAbsolutePath();
    private final FileClient client;
    private final OsUtils utils;

    @Inject
    public FileClientResource(@RestClient FileClient client) throws IOException {
        utils = OsUtils.get();
        utils.createFile(FILE.toString(), BIGGER_THAN_TWO_GIGABYTES);
        this.client = client;
    }

    @GET
    @Path("/client-hash")
    @Blocking
    public Uni<Response> calculateHash() {
        return Uni.createFrom().item(Response.ok(utils.getSum(FILE.toString())).build());
    }

    @GET
    @Path("/hash")
    public Uni<Response> hash() {
        return client.hash().map(hash -> Response.ok(hash).build());
    }

    @GET
    @Path("/download")
    public Uni<String> download() {
        return client.download().map(file -> {
            String path = file.getAbsolutePath();
            return utils.getSum(path);
        });
    }

    @GET
    @Path("/download-multipart")
    public String downloadMultipart() {
        FileWrapper wrapper = client.downloadMultipart();
        String path = wrapper.file.getAbsolutePath();
        return utils.getSum(path);
    }

    @POST
    @Path("/multipart")
    public Uni<String> uploadMultipart() {
        FileWrapper wrapper = new FileWrapper();
        wrapper.file = FILE.toFile();
        return client.sendMultipart(wrapper);
    }

    @POST
    @Path("/upload-file")
    public Uni<String> upload() {
        return client.sendFile(FILE.toFile());
    }
}
