package io.quarkus.ts.http.hibernate.validator.sources;

import java.util.ArrayList;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

@Path("/graal-vm-feature")
public class GraalVMFeatureResource {

    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    public String shortCircuitGreeting(@Valid @Size(min = 5, max = 5) ArrayList<String> list) {
        return "Number %d is alive".formatted(list.size());
    }

}
