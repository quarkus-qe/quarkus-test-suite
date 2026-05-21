package io.quarkus.ts.http.jakartarest.reactive.exceptions;

import org.jboss.resteasy.reactive.RestResponse;

public abstract class AbstractGenericExceptionMapper<E extends Exception> {

    public abstract RestResponse<?> toResponse(E e);
}
