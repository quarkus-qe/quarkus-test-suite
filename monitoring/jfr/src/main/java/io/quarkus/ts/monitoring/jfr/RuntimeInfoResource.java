package io.quarkus.ts.monitoring.jfr;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.builder.Version;
import io.quarkus.runtime.ImageMode;
import io.quarkus.runtime.configuration.ConfigUtils;

@Path("/runtime-info")
public class RuntimeInfoResource {

    @Path("/quarkus-version")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getQuarkusVersion() {
        return Version.getVersion();
    }

    @Path("/quarkus-image-mode")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getQuarkusImageMode() {
        return ImageMode.current().toString();
    }

    @Path("/quarkus-profiles")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getQuarkusProfile() {
        return String.join(",", ConfigUtils.getProfiles());
    }
}
