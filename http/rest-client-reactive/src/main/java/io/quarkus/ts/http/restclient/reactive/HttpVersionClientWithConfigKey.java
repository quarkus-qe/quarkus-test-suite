package io.quarkus.ts.http.restclient.reactive;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "http-client-with-config-key")
public interface HttpVersionClientWithConfigKey extends HttpVersionClient {
}
