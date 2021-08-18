package io.quarkus.ts.spring.cloud.config;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/jaxrs/hello")
public class JaxRsGreetingResource {

    @ConfigProperty(name = "custom.message")
    String message;

    @GET
    public String hello() {
        return message;
    }
}