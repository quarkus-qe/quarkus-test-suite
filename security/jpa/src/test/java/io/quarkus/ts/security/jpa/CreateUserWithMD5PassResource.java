package io.quarkus.ts.security.jpa;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import jakarta.xml.bind.DatatypeConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Path("/create/user")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class CreateUserWithMD5PassResource {
    @POST
    @Transactional
    public Response create(String jsonString) throws JsonProcessingException, NoSuchAlgorithmException {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        String username = jsonNode.get("username").asText();
        String password = jsonNode.get("password").asText();
        String role = jsonNode.get("role").asText();

        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        String md5Password = DatatypeConverter.printHexBinary(hash);

        MD5UserEntity user = new MD5UserEntity(username, md5Password, role);
        user.persist();
        return Response.ok(user).status(201).build();
    }
}
