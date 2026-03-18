package io.quarkus.ts.http.advanced.reactive.interceptor;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.NameBinding;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;

@Path("/media-type-writer-interceptor")
public class MediaTypeWriterInterceptorResource {

    @WithMediaTypeWriterInterceptor
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEntityThroughInterceptorChain() {
        return Response.ok(new CustomEntity()).build();
    }

    @NameBinding
    @Target({ ElementType.METHOD, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface WithMediaTypeWriterInterceptor {
    }

    public static class CustomEntity {
    }

    @Provider
    @WithMediaTypeWriterInterceptor
    public static class NoOpWriterInterceptor implements WriterInterceptor {

        @Override
        public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
            context.proceed();
        }
    }

    @Provider
    public static class CustomEntityMessageBodyWriter implements MessageBodyWriter<CustomEntity> {

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type.equals(CustomEntity.class);
        }

        @Override
        public void writeTo(CustomEntity entity, Class<?> type, Type genericType, Annotation[] annotations,
                MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
                throws IOException, WebApplicationException {
            if (mediaType == null) {
                throw new WebApplicationException("MediaType must not be null in WriterInterceptor chain",
                        Response.Status.INTERNAL_SERVER_ERROR);
            }
            if (!mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
                throw new WebApplicationException("Expected APPLICATION_JSON but got: " + mediaType,
                        Response.Status.INTERNAL_SERVER_ERROR);
            }
            entityStream.write("{\"message\":\"ok\"}".getBytes(StandardCharsets.UTF_8));
        }
    }
}
