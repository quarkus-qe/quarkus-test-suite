package com.redhat; // this bug can not be reproduced for classes in io.quarkus package

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import io.smallrye.common.annotation.Blocking;

/**
 * Reproducer for compilation failure
 * See https://github.com/quarkusio/quarkus/issues/38275#issuecomment-2115117993
 */
@RegisterRestClient
public interface BrokenBlockingApi {
    @GET
    @Path("/")
    String request();

    @Blocking
    @ClientExceptionMapper
    static RuntimeException toException(Response response) {
        String entity = response.readEntity(String.class).isEmpty()
                ? response.getStatusInfo().getReasonPhrase()
                : response.readEntity(String.class);
        return new RuntimeException(entity);
    }
}
