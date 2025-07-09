package io.quarkus.ts.http.restclient.reactive.exceptions;

import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

public class MyClientExceptionMapper implements ResponseExceptionMapper<MyCheckedException> {
    @Override
    public MyCheckedException toThrowable(Response response) {
        if (response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode()) {
            return new MyCheckedException("The remote service is unavailable");
        }
        return null;
    }
}
