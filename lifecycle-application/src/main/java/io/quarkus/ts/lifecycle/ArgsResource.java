package io.quarkus.ts.lifecycle;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.runtime.annotations.CommandLineArguments;

@Path("/args")
public class ArgsResource {
    @Inject
    @CommandLineArguments
    String[] args;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        return String.join(",", args);
    }
}
