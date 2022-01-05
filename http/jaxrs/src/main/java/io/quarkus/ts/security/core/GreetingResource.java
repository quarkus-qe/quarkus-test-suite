package io.quarkus.ts.security.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

@Path("/hello")
public class GreetingResource {

    private final ObjectMapper MAPPER = new JsonMapper();

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "hello";
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/serialize-string")
    public String serstring() throws IOException, ClassNotFoundException {

        byte[] bytes;

        try (
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos)) {

            oos.writeObject("Hello RESTEasy");
            oos.flush();
            bytes = baos.toByteArray();
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (String) ois.readObject();
        }
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/serialize-list")
    public String serlist() throws IOException, ClassNotFoundException {

        byte[] bytes;

        try (
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos)) {

            List<String> list = new ArrayList<>();
            list.add("Hello RESTEasy");

            oos.writeObject(list);
            oos.flush();
            bytes = baos.toByteArray();
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = new ObjectInputStream(bais)) {

            return ois.readObject().toString();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/big-serialize-list")
    public Map<String, Integer> bigSerlist(@QueryParam("expSize") int expSize) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int ix = 0;
        try (JsonGenerator g = MAPPER.createGenerator(out)) {
            g.writeStartArray();

            do {
                g.writeStartObject();
                g.writeNumberField("index", ix++);
                g.writeStringField("extra", "none#" + ix);
                g.writeEndObject();
            } while (out.size() < expSize);

            g.writeEndArray();
        }

        JsonNode root = MAPPER.readTree(out.toByteArray());

        byte[] ser = jdkSerialize(root);
        JsonNode result = jdkDeserialize(ser);
        return Map.of("size", result.size());
    }

    private byte[] jdkSerialize(Object o) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(1000);
        ObjectOutputStream obOut = new ObjectOutputStream(bytes);
        obOut.writeObject(o);
        obOut.close();
        return bytes.toByteArray();
    }

    private <T> T jdkDeserialize(byte[] raw) throws IOException, ClassNotFoundException {
        ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(raw));
        try {
            return (T) objIn.readObject();
        } catch (ClassNotFoundException e) {
            objIn.close();
            throw e;
        } finally {
            objIn.close();
        }
    }
}