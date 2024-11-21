package io.quarkus.ts.http.vertx.webclient.service;

import java.net.HttpURLConnection;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;

@ApplicationScoped
public class PongService {

    @Inject
    Vertx vertx;

    @ConfigProperty(name = "quarkus.http.port")
    public int port;

    private WebClient client;
    private String basePath;

    @PostConstruct
    void initialize() {
        this.client = WebClient.create(vertx);
        this.basePath = "http://localhost:" + port;
    }

    public Uni<String> pong() {
        return client.getAbs(basePath + "/chuck/pong")
                .putHeader("Accept", "application/json")
                .send()
                .map(res -> {
                    if (res.statusCode() != HttpURLConnection.HTTP_OK) {
                        throw new AssertionError("Unexpected HTTP status: " + res.statusCode());
                    }
                    return res;
                })
                .map(HttpResponse::bodyAsString);
    }
}
