package io.quarkus.ts.http.jakartarest;

import java.io.InputStream;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.providers.multipart.PartType;

public class MultipartBody {

    @FormParam("text")
    @PartType(MediaType.TEXT_PLAIN)
    public String text;

    @FormParam("image")
    @PartType("image/png")
    public InputStream image;

    @FormParam("data")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream data;
}
