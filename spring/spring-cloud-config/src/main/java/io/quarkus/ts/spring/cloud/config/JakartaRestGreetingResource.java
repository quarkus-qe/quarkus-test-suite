package io.quarkus.ts.spring.cloud.config;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/jakarta-rest/hello")
public class JakartaRestGreetingResource {

    @ConfigProperty(name = "custom.message")
    String message;

    @GET
    public String hello() {
        return message;
    }
}