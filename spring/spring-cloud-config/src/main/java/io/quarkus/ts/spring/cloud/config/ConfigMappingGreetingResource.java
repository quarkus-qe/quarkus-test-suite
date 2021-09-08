package io.quarkus.ts.spring.cloud.config;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/custom-mapping/hello")
public class ConfigMappingGreetingResource {

    @Inject
    CustomMessageConfig config;

    @GET
    public String hello() {
        return config.message();
    }
}