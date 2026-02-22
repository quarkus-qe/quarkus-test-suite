package io.quarkus.ts.openshift.security.basic.exceptions;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import io.quarkus.security.AuthenticationFailedException;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class TestAuthenticationFailedExceptionMapper
        implements ExceptionMapper<AuthenticationFailedException> {

    @Override
    public Response toResponse(AuthenticationFailedException exception) {
        return Response.status(Response.Status.UNAUTHORIZED)
                .header("Test-Auth-Exception", "AuthenticationFailedException")
                .build();
    }
}
