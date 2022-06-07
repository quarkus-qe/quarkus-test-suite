package io.quarkus.ts.http.advanced.reactive;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;

@Provider
public class MediaTypeWrapperSerializer implements MessageBodyWriter<MediaTypeWrapper> {

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
            MediaType mediaType) {
        return type == MediaTypeWrapper.class;
    }

    @Override
    public void writeTo(MediaTypeWrapper mediaTypeWrapper, Class<?> type,
            Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {
        mediaTypeWrapper.setMediaType(mediaType);
        entityStream.write(new ObjectMapper().writeValueAsBytes(mediaTypeWrapper));
    }
}
