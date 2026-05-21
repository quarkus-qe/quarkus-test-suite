package io.quarkus.ts.http.jakartarest.reactive.exceptions;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

@ApplicationScoped
public class GenericExceptionMapperBean extends AbstractGenericExceptionMapper<GenericMapperException> {

    @ServerExceptionMapper(GenericMapperException.class)
    public RestResponse<?> toResponse(GenericMapperException e) {
        return RestResponse.status(499);
    }
}
