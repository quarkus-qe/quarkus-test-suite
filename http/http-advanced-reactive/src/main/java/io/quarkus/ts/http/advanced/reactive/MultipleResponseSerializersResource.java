package io.quarkus.ts.http.advanced.reactive;

import static io.quarkus.ts.http.advanced.reactive.MultipleResponseSerializersResource.MULTIPLE_RESPONSE_SERIALIZERS_PATH;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static jakarta.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static jakarta.ws.rs.core.MediaType.TEXT_HTML;
import static jakarta.ws.rs.core.MediaType.TEXT_HTML_TYPE;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

import io.vertx.core.http.HttpServerRequest;

@Path(MULTIPLE_RESPONSE_SERIALIZERS_PATH)
public class MultipleResponseSerializersResource {

    public static final String MULTIPLE_RESPONSE_SERIALIZERS_PATH = "/multiple-response-serializers";

    /**
     * Response serializers are applied if request query param is true,
     * e.g. /multiple-response-serializers?apply-response-serializer=true
     */
    public static final String APPLY_RESPONSE_SERIALIZER_PARAM_FLAG = "apply-response-serializer";

    @GET
    @Produces({ TEXT_HTML, APPLICATION_OCTET_STREAM, TEXT_PLAIN, APPLICATION_JSON })
    public Response getMediaTypeAcceptedBySerializer() {
        // Actual response is streamed by a response type serializer
        return Response.ok("").build();
    }

    @Provider
    public static class TextHtmlSerializer extends StringResponseSerializer {

        protected TextHtmlSerializer() {
            super(TEXT_HTML_TYPE);
        }
    }

    @Provider
    public static class ApplicationOctetStreamSerializer extends StringResponseSerializer {

        protected ApplicationOctetStreamSerializer() {
            super(APPLICATION_OCTET_STREAM_TYPE);
        }
    }

    @Provider
    public static class TextPlainSerializer extends StringResponseSerializer {

        protected TextPlainSerializer() {
            super(TEXT_PLAIN_TYPE);
        }
    }

    @Provider
    public static class ApplicationJsonSerializer extends StringResponseSerializer {

        protected ApplicationJsonSerializer() {
            super(APPLICATION_JSON_TYPE);
        }
    }

    private static abstract class StringResponseSerializer implements MessageBodyWriter<String> {

        @Inject
        HttpServerRequest httpRequest;

        private final MediaType acceptedMediaType;

        protected StringResponseSerializer(MediaType acceptedMediaType) {
            this.acceptedMediaType = acceptedMediaType;
        }

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type.equals(String.class) && mediaType.isCompatible(acceptedMediaType)
            // Prevent applying a serializer unintentionally
                    && Boolean.parseBoolean(httpRequest.getParam(APPLY_RESPONSE_SERIALIZER_PARAM_FLAG));
        }

        @Override
        public void writeTo(String s, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
                throws IOException, WebApplicationException {
            entityStream.write(acceptedMediaType.toString().getBytes());
        }
    }
}
