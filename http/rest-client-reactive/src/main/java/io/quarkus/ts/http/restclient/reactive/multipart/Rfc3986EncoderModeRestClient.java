package io.quarkus.ts.http.restclient.reactive.multipart;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "encoder-mode-rfc3986")
public interface Rfc3986EncoderModeRestClient extends EncoderModeRestClient {

}
