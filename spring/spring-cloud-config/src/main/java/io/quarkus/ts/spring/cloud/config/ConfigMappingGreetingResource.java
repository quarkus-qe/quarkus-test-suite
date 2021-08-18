package io.quarkus.ts.spring.cloud.config;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/custom-mapping/hello")
public class ConfigMappingGreetingResource {

    // TODO Disabled because https://github.com/quarkusio/quarkus/issues/19448
    // @Inject
    CustomMessageConfig config;

    @GET
    public String hello() {
        return config.message();
    }
}