package io.quarkus.ts.http.advanced.reactive;

import java.util.HashMap;
import java.util.Map;

import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class JsonExceptionMapper implements ExceptionMapper<JsonbException> {

    @Override
    public Response toResponse(JsonbException e) {

        String message = e.getMessage();

        Map<String, Object> entity = new HashMap<>();
        entity.put("stack", message);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(entity)
                .build();
    }

}
