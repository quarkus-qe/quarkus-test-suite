package io.quarkus.ts.http.restclient.reactive.files;

import java.io.File;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.reactive.PartType;

public class FileWrapper {
    @FormParam("file")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    public File file;
}
