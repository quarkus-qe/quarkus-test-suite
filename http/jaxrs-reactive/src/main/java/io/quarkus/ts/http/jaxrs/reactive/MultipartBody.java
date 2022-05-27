package io.quarkus.ts.http.jaxrs.reactive;

import java.io.File;

import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;

public class MultipartBody {

    @RestForm("text")
    @PartType(MediaType.TEXT_PLAIN)
    public String text;

    @RestForm("image")
    @PartType("image/png")
    public File image;

    @RestForm("data")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    public File data;
}
