package io.quarkus.ts.http.advanced.reactive;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class Brotli4JRestMock {

    private static final Logger LOGGER = Logger.getLogger(Brotli4JRestMock.class);
    @Inject
    private ObjectMapper objectMapper;

    private static HashMap<String, Object> response = null;

    @PostConstruct
    public void init() {
        try (InputStream inputStream = getClass().getResourceAsStream("/json_sample.json")) {
            byte[] bytes = inputStream.readAllBytes();
            response = objectMapper.readValue(bytes, HashMap.class);
        } catch (StreamReadException | DatabindException e) {
            LOGGER.error("Error occurred while deserializing JSON file {}" + e.getMessage());
        } catch (IOException e) {
            LOGGER.error("Error occurred while reading the JSON file {} " + e.getMessage());
        }
    }

    public HashMap<String, Object> returnResponse() {
        return response;
    }

}