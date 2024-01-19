package io.quarkus.ts.http.restclient.vanilla;

import java.net.URI;

import jakarta.annotation.PostConstruct;

import org.eclipse.microprofile.rest.client.RestClientBuilder;

import io.quarkus.runtime.Startup;

/**
 * This class is used to reproduce issue https://github.com/quarkusio/quarkus/issues/31024
 * Also requires {@link UselessRestApi} and {@link VersionHeaderFilter} for this
 */
@Startup
public class RestCallerService {

    @PostConstruct
    void initRestApi() {
        RestClientBuilder builder = RestClientBuilder.newBuilder()//
                .baseUri(URI.create("localhost"));

        // API needs to be created for issue to manifest
        UselessRestApi api = builder.build(UselessRestApi.class);
    }
}
