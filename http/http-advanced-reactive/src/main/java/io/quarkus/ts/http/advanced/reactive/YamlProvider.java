package io.quarkus.ts.http.advanced.reactive;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import io.quarkus.logging.Log;

@Provider
@Consumes("application/yaml")
@Produces("application/yaml")
public class YamlProvider implements MessageBodyReader<CityListDTO>, MessageBodyWriter<CityListDTO> {

    private final ObjectMapper mapper;

    public YamlProvider() {
        System.out.println("calling YAMLPROVIDER");
        this.mapper = new ObjectMapper(new YAMLFactory());
    }

    @Override
    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    @Override
    public CityListDTO readFrom(Class<CityListDTO> cityListDTOClass, Type type, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> multivaluedMap,
            InputStream inputStream) {

        try {
            Log.info("Parsing yaml input: " + inputStream.toString() + " TYPE " + type.getTypeName() + " MEDIATYPE "
                    + mediaType.getSubtype());
            return this.mapper.readValue(inputStream, CityListDTO.class);
        } catch (Exception e) {
            Log.error("Error reading YAML input", e);
            throw new WebApplicationException("Error reading YAML input", e);
        }
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    @Override
    public void writeTo(CityListDTO cityListDTO, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> multivaluedMap,
            OutputStream outputStream) throws IOException, WebApplicationException {
        Log.info("outputStream " + outputStream.toString() + " TYPE " + type.getTypeName() + " MEDIATYPE "
                + mediaType.getSubtype());
        mapper.writeValue(outputStream, cityListDTO);
    }

}
