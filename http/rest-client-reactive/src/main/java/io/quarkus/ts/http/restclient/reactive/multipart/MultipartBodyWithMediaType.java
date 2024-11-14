package io.quarkus.ts.http.restclient.reactive.multipart;

import java.io.InputStream;

import jakarta.ws.rs.core.MediaType;

import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;

public class MultipartBodyWithMediaType {
    @RestForm("file")
    public InputStream file;

    @RestForm("fileName")
    @PartType(MediaType.TEXT_PLAIN)
    public String fileName;
}
