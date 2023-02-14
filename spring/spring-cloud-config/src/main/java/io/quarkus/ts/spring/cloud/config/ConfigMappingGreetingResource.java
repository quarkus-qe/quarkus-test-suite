package io.quarkus.ts.spring.cloud.config;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/custom-mapping/hello")
public class ConfigMappingGreetingResource {

    @Inject
    CustomMessageConfig config;

    @GET
    public String hello() {
        return config.message();
    }
}