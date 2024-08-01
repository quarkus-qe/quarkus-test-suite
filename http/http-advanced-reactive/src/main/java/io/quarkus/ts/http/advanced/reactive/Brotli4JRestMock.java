package io.quarkus.ts.http.advanced.reactive;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Scanner;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class Brotli4JRestMock {

    private static final Logger LOGGER = Logger.getLogger(Brotli4JRestMock.class);

    private static HashMap<String, Object> jsonResponse = null;
    private static HashMap<String, Object> xmlResponse = null;

    private String textResponse = "";
    private String bigTextResponse = "";

    private final ObjectMapper objectMapper;
    private final String textPlainFilePath;
    private final String jsonFilePath;
    private final String xmlFilePath;
    private final String textBigPlainFilePath;

    public Brotli4JRestMock(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.textPlainFilePath = "/sample.txt";
        this.jsonFilePath = "/sample.json";
        this.xmlFilePath = "/sample.xml";
        this.textBigPlainFilePath = "/big_sample.txt";
    }

    @PostConstruct
    public void init() {
        loadJsonFile(jsonFilePath);
        loadXmlResponse(xmlFilePath);
        textResponse = loadTextData(textPlainFilePath);
        bigTextResponse = loadTextData(textBigPlainFilePath);
    }

    private void loadJsonFile(String jsonFilePath) {
        try (InputStream inputStream = getClass().getResourceAsStream(jsonFilePath)) {
            assert inputStream != null;
            byte[] bytes = inputStream.readAllBytes();
            jsonResponse = objectMapper.readValue(bytes, new TypeReference<>() {
            });
        } catch (StreamReadException | DatabindException e) {
            LOGGER.error("Error occurred while deserializing JSON file: " + e.getMessage());
        } catch (IOException e) {
            LOGGER.error("Error occsurred while reading the JSON file: " + e.getMessage());
        }
    }

    private void loadXmlResponse(String xmlFilePath) {
        try (InputStream inputStream = getClass().getResourceAsStream(xmlFilePath)) {
            assert inputStream != null;
            Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8);
            StringBuilder stringBuilder = new StringBuilder();
            while (scanner.hasNextLine()) {
                stringBuilder.append(scanner.nextLine()).append("\n");
            }
            xmlResponse = new HashMap<>();
            xmlResponse.put("xml", stringBuilder.toString());
        } catch (IOException e) {
            throw new RuntimeException("Error loading XML response", e);
        }
    }

    public HashMap<String, Object> returnResponse(ResponseType type) {
        if (type == ResponseType.JSON) {
            return jsonResponse;
        } else if (type == ResponseType.XML) {
            return xmlResponse;
        } else {
            throw new IllegalArgumentException("Invalid response type: " + type);
        }
    }

    public String loadTextData(String filePath) {
        try (InputStream inputStream = getClass().getResourceAsStream(filePath)) {
            if (inputStream != null) {
                byte[] textData = inputStream.readAllBytes();
                return new String(textData, StandardCharsets.UTF_8);
            } else {
                throw new IOException("File not found: " + filePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading text data from file: " + filePath, e);
        }
    }

    public String returnTextPlainResponse(ResponseType type) {
        if (type == ResponseType.TEXT) {
            return textResponse;
        } else if (type == ResponseType.BIG_TEXT) {
            return bigTextResponse;
        } else {
            throw new IllegalArgumentException("Invalid response type: " + type);
        }
    }

    public enum ResponseType {
        JSON,
        XML,
        TEXT,
        BIG_TEXT
    }

}
