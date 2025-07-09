package io.quarkus.ts.http.restclient.reactive.fault.tolerance;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.quarkus.ts.http.restclient.reactive.exceptions.MyCheckedException;
import io.quarkus.ts.http.restclient.reactive.exceptions.MyClientExceptionMapper;

@Path("/service-unavailable")
@RegisterRestClient(configKey = "checked-exception-client")
@RegisterProvider(MyClientExceptionMapper.class)
public interface FaultToleranceExceptionMappingClient {
    @GET
    @Retry(maxRetries = 1)
    String get() throws MyCheckedException;

}
