package io.quarkus.ts.http.reactiveroutes.validation;

import javax.validation.Valid;

import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.Route.HttpMethod;
import io.quarkus.vertx.web.RouteBase;
import io.smallrye.mutiny.Uni;

@RouteBase(path = "/validate")
public class ValidationOnResponseRouteHandler {

    @Route(methods = HttpMethod.GET, path = "/response-uni-valid")
    @Valid
    Uni<Response> validateUniResponseWithTypeReturnsValid() {
        return Uni.createFrom().item(createResponse());
    }

    @Route(methods = HttpMethod.GET, path = "/response-uni-invalid-id")
    @Valid
    Uni<Response> validateUniResponseWithTypeReturnsInvalidID() {
        Response response = createResponse();
        response.setId(null); // it's not invalid!
        return Uni.createFrom().item(response);
    }

    private static final Response createResponse() {
        Response response = new Response();
        response.setId("identifier");
        response.setCustom("UPPER");
        return response;
    }
}
