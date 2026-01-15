package io.quarkus.ts.monitoring.jfr;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/app-info")
public class AppInfoResource {

    @ConfigProperty(name = "quarkus.application.version")
    String appVersion;

    @ConfigProperty(name = "quarkus.application.name")
    String appName;

    @Path("/app-version")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getAppVersion() {
        return appVersion;
    }

    @Path("/app-name")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getAppName() {
        return appName;
    }
}
