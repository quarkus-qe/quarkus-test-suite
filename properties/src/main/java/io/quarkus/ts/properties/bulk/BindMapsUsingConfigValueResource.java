package io.quarkus.ts.properties.bulk;

import java.util.Map;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/bind-maps-using-config-value")
public class BindMapsUsingConfigValueResource {

    @ConfigProperty(name = "maps.labels")
    Map<String, String> labels;

    @ConfigProperty(name = "maps.numbers")
    Map<Integer, Integer> numbers;

    @GET
    @Path("/labels/{label}")
    public String getLabel(@PathParam("label") String label) {
        return labels.get(label);
    }

    @GET
    @Path("/numbers/{number}")
    public Integer getNumber(@PathParam("number") Integer number) {
        return numbers.get(number);
    }
}
