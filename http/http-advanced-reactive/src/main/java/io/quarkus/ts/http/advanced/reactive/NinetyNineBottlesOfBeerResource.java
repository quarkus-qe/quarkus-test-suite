package io.quarkus.ts.http.advanced.reactive;

import static io.quarkus.ts.http.advanced.reactive.NinetyNineBottlesOfBeerResource.ENABLED_ON_QUARKUS_2_8_3_OR_HIGHER_NAME;
import static io.quarkus.ts.http.advanced.reactive.NinetyNineBottlesOfBeerResource.ENABLED_ON_QUARKUS_2_8_3_OR_HIGHER_VAL;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.Uni;

/**
 * {@link Path#value()} set in this resource contains characters that were causing the build time
 * validation failure. The issue was resolved in 2.8.3. with https://github.com/quarkusio/quarkus/issues/25258.
 */
@RegisterForReflection
@IfBuildProperty(name = ENABLED_ON_QUARKUS_2_8_3_OR_HIGHER_NAME, stringValue = ENABLED_ON_QUARKUS_2_8_3_OR_HIGHER_VAL)
@Path(NinetyNineBottlesOfBeerResource.PATH)
public class NinetyNineBottlesOfBeerResource {

    public static final String ENABLED_ON_QUARKUS_2_8_3_OR_HIGHER_NAME = "quarkus-platform-version-2.8.3-or-higher";
    public static final String ENABLED_ON_QUARKUS_2_8_3_OR_HIGHER_VAL = "true";
    public static final String FIRST_BOTTLE_RESPONSE = "1 bottle of beer on the wall, 1 bottle of beer. Take one down, pass it around, no more beer on the wall!";
    public static final String SECOND_BOTTLE_RESPONSE = "2 bottles of beer on the wall, 2 bottles of beer. Take one down, pass it around, 1 bottle of beer on the wall.";
    public static final String OTHER_BOTTLES_RESPONSE = "%d bottles of beer on the wall, %d bottles of beer. Take one down, pass it around, %d bottles of beer on the wall.";
    public static final String PATH = "/99-bottles-of-beer/bottle";
    private static final String EXCEPTION_MESSAGE = "%s: expected path parameter value '%s' differ from '%s'.";

    @Produces(APPLICATION_JSON)
    @GET
    @Path("/{bottle-number:1}")
    public Uni<String> getFirstBottle(@PathParam("bottle-number") Integer bottleNumber) {
        if (bottleNumber != null && bottleNumber.equals(1)) {
            return Uni.createFrom().item(FIRST_BOTTLE_RESPONSE);
        }
        throw new IllegalStateException(String.format(EXCEPTION_MESSAGE, "getFirstBottle", 1, bottleNumber));
    }

    @Produces(APPLICATION_JSON)
    @GET
    @Path("/{2-nd-bottle-number:[^0-1,3-9]}")
    public Uni<String> getSecondBottle(@PathParam("2-nd-bottle-number") Integer bottleNumber) {
        if (bottleNumber != null && bottleNumber.equals(2)) {
            return Uni.createFrom().item(SECOND_BOTTLE_RESPONSE);
        }
        throw new IllegalStateException(String.format(EXCEPTION_MESSAGE, "getSecondBottle", 2, bottleNumber));
    }

    @Produces(APPLICATION_JSON)
    @GET
    @Path("/{_bottle.number:[3-9]|[1-9][0-9]}")
    public Uni<String> getOtherBottles(@PathParam("_bottle.number") Integer bottleNumber) {
        if (bottleNumber != null && bottleNumber >= 3 && bottleNumber <= 99) {
            return Uni.createFrom().item(String.format(OTHER_BOTTLES_RESPONSE, bottleNumber, bottleNumber, bottleNumber - 1));
        }
        throw new IllegalStateException(String.format(EXCEPTION_MESSAGE, "getOtherBottles", "3-99", bottleNumber));
    }
}
