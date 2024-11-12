package io.quarkus.ts.http.advanced.reactive;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.Path;

@Path("/greeting")
public class GreetingOptionAndHeadResource implements GreetingInterface {

    @Path("/cdi-sub-resource")
    public GreetingSubResources helloFromSubResource() {
        return CDI.current()
                .select(GreetingSubResources.class)
                .get();
    }

    @Override
    public String interfaceGreeting() {
        return "Greeting from interface";
    }
}
