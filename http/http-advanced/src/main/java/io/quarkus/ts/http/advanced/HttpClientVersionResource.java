package io.quarkus.ts.http.advanced;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;

import io.quarkus.vertx.web.Route;
import io.vertx.ext.web.RoutingContext;

@ApplicationScoped
public class HttpClientVersionResource {

    protected static final String HTTP_VERSION = "x-http-version";

    @Route(methods = Route.HttpMethod.GET, path = "/httpVersion")
    public void clientHttpVersion(RoutingContext rc) {
        String httpClientVersion = rc.request().version().name();
        rc.response().headers().add(HTTP_VERSION, httpClientVersion);
        rc.response().setStatusCode(Response.Status.OK.getStatusCode()).end();
    }
}
