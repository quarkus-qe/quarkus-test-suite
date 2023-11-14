package io.quarkus.ts.http.advanced.reactive;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.ConstrainedTo;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NameBinding;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;

@Path("/intercepted")
public class InterceptedResource {

    /**
     * Interceptors write their message to this list, when they are invoked
     * It is a bit dumb way, but it is the easier to get indicators if interceptors were invoked to the client
     */
    public static List<String> interceptorMessages = new ArrayList<>();

    @WithWriterInterceptor
    @GET
    public InterceptedString getInterceptedString() {
        return new InterceptedString("foo");
    }

    @GET()
    @Path("/messages")
    @Produces(MediaType.TEXT_PLAIN)
    public String getMessages() {
        StringBuilder outputMessage = new StringBuilder();
        for (String string : interceptorMessages) {
            outputMessage.append(string);
        }
        return outputMessage.toString();
    }

    public static class InterceptedString {
        public String name;

        public InterceptedString(String name) {
            this.name = name;
        }
    }

    /**
     * This annotation binds the providers to only intercept the method in this class.
     * Otherwise, they would be global and intercept all endpoints across the entire application.
     */
    @NameBinding
    @Target({ ElementType.METHOD, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface WithWriterInterceptor {

    }

    @Provider
    public static class InterceptedStringHandler implements MessageBodyWriter<InterceptedString> {
        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type == InterceptedString.class;
        }

        @Override
        public void writeTo(InterceptedString interceptedString, Class<?> type, Type genericType, Annotation[] annotations,
                MediaType mediaType,
                MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
                throws IOException, WebApplicationException {
            entityStream.write((interceptedString.name).getBytes(StandardCharsets.UTF_8));
        }
    }

    @Provider
    @WithWriterInterceptor
    public static class UnconstrainedWriterInterceptor implements WriterInterceptor {
        @Override
        public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
            InterceptedResource.interceptorMessages.add("Unconstrained interceptor ");
            context.proceed();
        }
    }

    @Provider
    @ConstrainedTo(RuntimeType.CLIENT)
    @WithWriterInterceptor
    public static class ClientWriterInterceptor implements WriterInterceptor {

        @Override
        public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
            InterceptedResource.interceptorMessages.add("Client interceptor ");
            context.proceed();
        }
    }

    @Provider
    @ConstrainedTo(RuntimeType.SERVER)
    @WithWriterInterceptor
    public static class ServerWriterInterceptor implements WriterInterceptor {

        @Override
        public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
            InterceptedResource.interceptorMessages.add("Server interceptor ");
            context.proceed();
        }
    }
}
