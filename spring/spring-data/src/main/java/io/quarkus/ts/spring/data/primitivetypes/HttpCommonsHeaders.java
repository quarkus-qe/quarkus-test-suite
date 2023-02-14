package io.quarkus.ts.spring.data.primitivetypes;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;

import io.quarkus.ts.spring.data.primitivetypes.configuration.AppConfiguration;
import io.quarkus.ts.spring.data.primitivetypes.configuration.InstanceIdBean;
import io.quarkus.ts.spring.data.primitivetypes.configuration.RequestIdBean;
import io.quarkus.ts.spring.data.primitivetypes.configuration.SessionIdBean;

@Provider
public class HttpCommonsHeaders implements ContainerResponseFilter {

    @Inject
    InstanceIdBean instanceId;

    @Inject
    RequestIdBean requestIdBean;

    @Inject
    SessionIdBean sessionIdBean;

    @Override
    public void filter(ContainerRequestContext requestCtx, ContainerResponseContext responseCtx) {
        MultivaluedMap<String, Object> headers = responseCtx.getHeaders();
        headers.add("x-count", AppConfiguration.getAndIncIndex());
        headers.add("x-session", sessionIdBean.getSessionId());
        headers.add("x-request", requestIdBean.getRequestId());
        headers.add("x-instance", instanceId.getInstanceId());
    }
}
