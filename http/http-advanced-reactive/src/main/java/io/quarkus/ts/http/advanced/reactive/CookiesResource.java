package io.quarkus.ts.http.advanced.reactive;

import java.util.Map;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.common.headers.NewCookieHeaderDelegate;
import org.jboss.resteasy.reactive.server.jaxrs.RestResponseBuilderImpl;

import io.vertx.core.http.Cookie;
import io.vertx.ext.web.RoutingContext;

@Path("/cookie")
public class CookiesResource {

    public static final String TEST_COOKIE = "test-cookie";

    @POST
    @Path("/same-site/cookie-param")
    public RestResponse<String> getSameSiteAttributeFromCookieParam(@CookieParam(TEST_COOKIE) String sameSite) {
        String rawCookie = toRawCookie(sameSite);
        NewCookie newCookie = (NewCookie) NewCookieHeaderDelegate.INSTANCE.fromString(rawCookie);
        return new RestResponseBuilderImpl<String>()
                .cookie(newCookie)
                .entity(getSameSite(newCookie))
                .build();
    }

    @GET
    @Path("/same-site/vertx")
    public Response getSameSiteAttributeFromVertx(HttpHeaders httpHeaders, RoutingContext routingContext) {
        routingContext.response().addCookie(Cookie.cookie("vertx", "ignored"));
        var sameSite = httpHeaders.getCookies().get(TEST_COOKIE).getValue();
        return Response.ok().entity(sameSite).build();
    }

    @POST
    @Path("/same-site/form-param")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getSameSiteAttributeFromFormParam(@FormParam(TEST_COOKIE) String cookie) {
        NewCookie newCookie = (NewCookie) NewCookieHeaderDelegate.INSTANCE.fromString(cookie);
        final Response.ResponseBuilder responseBuilder;
        if (newCookie.getSameSite() == null) {
            responseBuilder = Response.noContent();
        } else {
            responseBuilder = Response.ok(getSameSite(newCookie));
        }
        return responseBuilder.cookie(newCookie).build();
    }

    @GET
    @Path("newcookie-serialization")
    public Map<String, NewCookie> getRequestCookies(HttpHeaders httpHeaders) {
        NewCookie cookie = new NewCookie.Builder(TEST_COOKIE).value("test-cookie-value").build();
        return Map.of(cookie.getName(), cookie);
    }

    @GET
    @Path("cookie-serialization")
    public Map<String, jakarta.ws.rs.core.Cookie> test(HttpHeaders httpHeaders) {
        return httpHeaders.getCookies();
    }

    public static String toRawCookie(String sameSite) {
        if (sameSite == null || sameSite.isEmpty()) {
            return String.format("%s=\"test-cookie-value\";Version=\"1\";", TEST_COOKIE);
        } else {
            return String.format("%s=\"test-cookie-value\";SameSite=\"%s\";Version=\"1\";", TEST_COOKIE, sameSite);
        }
    }

    private static String getSameSite(NewCookie cookie) {
        if (cookie.getSameSite() == null) {
            return null;
        } else {
            return cookie.getSameSite().toString();
        }
    }

}
