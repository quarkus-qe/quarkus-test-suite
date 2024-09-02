package io.quarkus.ts.http.advanced.reactive;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class HeadersMessageBodyWriter implements MessageBodyWriter<CustomHeaderResponse> {

    @Override
    public boolean isWriteable(Class aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return CustomHeaderResponse.class.isAssignableFrom(aClass) && MediaType.TEXT_PLAIN_TYPE.isCompatible(mediaType);
    }

    @Override
    public void writeTo(CustomHeaderResponse customHeaderResponse, Class<?> aClass, Type type, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap, OutputStream outputStream)
            throws IOException, WebApplicationException {
        final String content = "Headers response: " + customHeaderResponse.getContent();
        outputStream.write(content.getBytes());
    }
}
