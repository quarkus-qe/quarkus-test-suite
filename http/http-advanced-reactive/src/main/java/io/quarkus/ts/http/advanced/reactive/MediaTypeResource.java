package io.quarkus.ts.http.advanced.reactive;

import static io.quarkus.ts.http.advanced.reactive.MediaTypeResource.MEDIA_TYPE_PATH;
import static javax.ws.rs.core.HttpHeaders.ACCEPT_ENCODING;
import static javax.ws.rs.core.HttpHeaders.ACCEPT_LANGUAGE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import io.quarkus.vertx.web.RouteFilter;
import io.vertx.ext.web.RoutingContext;

@Path(MEDIA_TYPE_PATH)
public class MediaTypeResource {

    public static final String MEDIA_TYPE_PATH = "/media-type";
    private static final String IMAGE_PNG = "image/png";
    private static final String IMAGE_JPEG = "image/jpeg";
    private static final String TEXT_CSS = "text/css";
    private static final String TEXT_XML = "text/xml";
    public static final String APPLICATION_YAML = "application/yaml";
    public static final String ENGLISH = "en";
    public static final String JAPANESE = "ja";
    public static final String ANY_ENCODING = "*";

    @Produces({ APPLICATION_JSON, APPLICATION_XML, TEXT_HTML, TEXT_PLAIN, APPLICATION_OCTET_STREAM,
            MULTIPART_FORM_DATA, IMAGE_PNG, IMAGE_JPEG, APPLICATION_YAML, TEXT_CSS, TEXT_XML })
    @GET
    public Response getMediaType() {
        return Response.ok(new MediaTypeWrapper()).build();
    }

    public static class ContentNegotiationRoutingFilter {
        @RouteFilter
        void addHeaders(final RoutingContext rc) {
            rc.response().headers().add(HttpHeaders.VARY, ACCEPT_LANGUAGE);
            rc.response().headers().add(ACCEPT_LANGUAGE, ENGLISH);
            rc.next();
        }
    }

    @Provider
    public static class ContentNegotiationContainerResponseFilter implements
            ContainerResponseFilter {
        @Override
        public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext)
                throws IOException {
            responseContext.getHeaders().add(HttpHeaders.VARY, ACCEPT_ENCODING);
            responseContext.getHeaders().add(ACCEPT_ENCODING, ANY_ENCODING);
            responseContext.getHeaders().add(ACCEPT_LANGUAGE, JAPANESE);
        }
    }
}
