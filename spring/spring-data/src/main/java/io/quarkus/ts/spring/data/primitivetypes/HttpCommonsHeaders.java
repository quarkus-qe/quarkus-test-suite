package io.quarkus.ts.spring.data.primitivetypes;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

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
