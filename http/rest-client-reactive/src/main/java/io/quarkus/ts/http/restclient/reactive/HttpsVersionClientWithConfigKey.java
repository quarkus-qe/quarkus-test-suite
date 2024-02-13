package io.quarkus.ts.http.restclient.reactive;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "https-client-with-config-key")
public interface HttpsVersionClientWithConfigKey extends HttpVersionClient {
}
