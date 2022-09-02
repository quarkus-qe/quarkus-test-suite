package io.quarkus.ts.http.restclient.reactive.files;

import java.io.File;

import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class FileWrapper {
    @RestForm("file")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    public File file;

    @RestForm("name")
    @PartType(MediaType.TEXT_PLAIN)
    public String name;
}
