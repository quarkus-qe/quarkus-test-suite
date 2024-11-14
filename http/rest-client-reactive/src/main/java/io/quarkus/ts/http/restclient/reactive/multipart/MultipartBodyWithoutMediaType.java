package io.quarkus.ts.http.restclient.reactive.multipart;

import java.io.InputStream;

import org.jboss.resteasy.reactive.RestForm;

public class MultipartBodyWithoutMediaType {
    @RestForm("file")
    public InputStream file;

    @RestForm("fileName")
    public String fileName;
}
