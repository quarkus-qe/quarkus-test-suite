package io.quarkus.ts.http.restclient.reactive.validation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
public interface UserProcessingService {

    @POST
    @Consumes(value = MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ValidatedUser process(
            @Valid @ConvertGroup(to = ValidatedUser.ConvertedGroup.class) @NotNull ValidatedUser entity);
}
