package io.quarkus.ts.http.jakartarest.reactive.json;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import com.fasterxml.jackson.annotation.JsonView;

import io.quarkus.resteasy.reactive.jackson.CustomSerialization;

@Path("/json")
public class JsonResource {
    public static final Integer USER_ID = 1;
    public static final String USER_NAME = "testUser";

    @JsonView(Views.Public.class)
    @GET
    @Path("/public")
    public User userPublic() {
        return testUser();
    }

    @JsonView(Views.Private.class)
    @GET
    @Path("/private")
    public User userPrivate() {
        return testUser();
    }

    @CustomSerialization(UnquotedFields.class)
    @GET
    @Path("/custom")
    public User userCustom() {
        return testUser();
    }

    private User testUser() {
        User user = new User();
        user.id = USER_ID;
        user.name = USER_NAME;
        return user;
    }
}
