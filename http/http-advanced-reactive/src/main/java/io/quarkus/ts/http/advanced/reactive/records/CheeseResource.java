package io.quarkus.ts.http.advanced.reactive.records;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import org.jboss.resteasy.reactive.RestCookie;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestHeader;
import org.jboss.resteasy.reactive.RestMatrix;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;

@Path("/cheese")
public class CheeseResource {
    @GET
    @Path("/quarkus/{type}")
    public String paramQuarkus(ParametersQuarkus p, @RestHeader String headerParam) {
        return headerParam + "/" + p;
    }

    @GET
    @Path("/spec/{type}")
    public String paramSpec(ParametersSpec p, @HeaderParam("Header-Param") String headerParam) {
        return headerParam + "/" + p;
    }

    @GET
    @Path("/class/{type}")
    public String paramClass(ParametersClass p, @RestHeader String headerParam) {
        return headerParam + "/" + p;
    }

    @POST
    @Path("/quarkus/{type}")
    public String paramQuarkusPost(@Valid ParametersQuarkus p, @RestHeader String headerParam) {
        return headerParam + "/" + p;
    }

    @POST
    @Path("/spec/{type}")
    public String paramSpecPost(@Valid ParametersSpec p, @HeaderParam("Header-Param") String headerParam) {
        return headerParam + "/" + p;
    }

    @POST
    @Path("/class/{type}")
    public String paramClassPost(@Valid ParametersClass p, @RestHeader String headerParam) {
        return headerParam + "/" + p;
    }

    public record ParametersQuarkus(
            @RestPath String type,
            @RestMatrix String variant,
            @RestQuery String age,
            @NotBlank(message = "Level may not be blank") @RestCookie String level,
            @RestHeader("X-Cheese-Secret-Handshake") String secretHandshake,
            @RestForm String smell,
            AgeRecord ar,
            AgeClass ac) {
    }

    public record ParametersSpec(
            @PathParam("type") String type,
            @MatrixParam("variant") String variant,
            @QueryParam("age") String age,
            @NotBlank(message = "Level may not be blank") @CookieParam("level") @NotBlank String level,
            @HeaderParam("X-Cheese-Secret-Handshake") String secretHandshake,
            @FormParam("smell") String smell,
            AgeRecord ar,
            AgeClass ac) {
    }

    public static class ParametersClass {
        @RestPath
        String type;
        @RestMatrix
        String variant;
        @RestQuery
        String age;
        @NotBlank(message = "Level may not be blank")
        @RestCookie
        String level;
        @RestHeader("X-Cheese-Secret-Handshake")
        String secretHandshake;
        @RestForm
        String smell;
        @BeanParam
        AgeRecord ar;
        @BeanParam
        AgeClass ac;

        @Override
        public String toString() {
            return "ParametersClass{" +
                    "type=" + type +
                    ", variant=" + variant +
                    ", age=" + age +
                    ", level=" + level +
                    ", secretHandshake=" + secretHandshake +
                    ", smell=" + smell +
                    ", ar=" + ar +
                    ", ac=" + ac +
                    '}';
        }
    }

    public record AgeRecord(@RestQuery String age) {
    }

    public static class AgeClass {
        @RestQuery
        String age;

        @Override
        public String toString() {
            return "AgeClass{" +
                    "age=" + age +
                    '}';
        }
    }
}
