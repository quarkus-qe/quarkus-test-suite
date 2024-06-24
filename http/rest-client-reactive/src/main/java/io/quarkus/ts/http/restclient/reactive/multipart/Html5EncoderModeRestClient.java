package io.quarkus.ts.http.restclient.reactive.multipart;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "encoder-mode-html5")
public interface Html5EncoderModeRestClient extends EncoderModeRestClient {

}