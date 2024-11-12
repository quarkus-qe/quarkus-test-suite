package io.quarkus.ts.http.advanced.reactive;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;

@RequestScoped
public class GreetingSubResources {

    @GET
    public String get() {
        return "Greeting from sub-resource using GET";
    }

    @POST
    public String post() {
        return "Greeting from sub-resource using POST";
    }

}
