package io.quarkus.ts.many.extensions;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;

@QuarkusScenario
public class ManyExtensionsIT {

    @Test
    public void httpServer() {
        given().get("/api/hello").then().statusCode(HttpStatus.SC_OK).body("content", is("Hello, World!"));
    }

    @Test
    public void testResourceShowy() {
        given().get("/api/resource/showy/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Showy - foo - done"));
    }

    @Test
    public void testResourceTricky() {
        given().get("/api/resource/tricky/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Tricky - foo - done"));
    }

    @Test
    public void testResourceObsolete() {
        given().get("/api/resource/obsolete/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Obsolete - foo - done"));
    }

    @Test
    public void testResourcePassenger() {
        given().get("/api/resource/passenger/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Passenger - foo - done"));
    }

    @Test
    public void testResourceDisastrous() {
        given().get("/api/resource/disastrous/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Disastrous - foo - done"));
    }

    @Test
    public void testResourceZany() {
        given().get("/api/resource/zany/foo").then().statusCode(HttpStatus.SC_OK).body("content", is("Zany - foo - done"));
    }

    @Test
    public void testResourceSlim() {
        given().get("/api/resource/slim/foo").then().statusCode(HttpStatus.SC_OK).body("content", is("Slim - foo - done"));
    }

    @Test
    public void testResourcePeachy() {
        given().get("/api/resource/peachy/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Peachy - foo - done"));
    }

    @Test
    public void testResourceAmused() {
        given().get("/api/resource/amused/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Amused - foo - done"));
    }

    @Test
    public void testResourceUnnatural() {
        given().get("/api/resource/unnatural/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Unnatural - foo - done"));
    }

    @Test
    public void testResourceHelpful() {
        given().get("/api/resource/helpful/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Helpful - foo - done"));
    }

    @Test
    public void testResourceAmbitious() {
        given().get("/api/resource/ambitious/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Ambitious - foo - done"));
    }

    @Test
    public void testResourceLarge() {
        given().get("/api/resource/large/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Large - foo - done"));
    }

    @Test
    public void testResourceMean() {
        given().get("/api/resource/mean/foo").then().statusCode(HttpStatus.SC_OK).body("content", is("Mean - foo - done"));
    }

    @Test
    public void testResourceShaggy() {
        given().get("/api/resource/shaggy/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Shaggy - foo - done"));
    }

    @Test
    public void testResourceOpposite() {
        given().get("/api/resource/opposite/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Opposite - foo - done"));
    }

    @Test
    public void testResourceGentle() {
        given().get("/api/resource/gentle/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Gentle - foo - done"));
    }

    @Test
    public void testResourceSmelly() {
        given().get("/api/resource/smelly/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Smelly - foo - done"));
    }

    @Test
    public void testResourceAquatic() {
        given().get("/api/resource/aquatic/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Aquatic - foo - done"));
    }

    @Test
    public void testResourceSteady() {
        given().get("/api/resource/steady/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Steady - foo - done"));
    }

    @Test
    public void testResourceRelieved() {
        given().get("/api/resource/relieved/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Relieved - foo - done"));
    }

    @Test
    public void testResourceGroovy() {
        given().get("/api/resource/groovy/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Groovy - foo - done"));
    }

    @Test
    public void testResourceSable() {
        given().get("/api/resource/sable/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Sable - foo - done"));
    }

    @Test
    public void testResourceDirty() {
        given().get("/api/resource/dirty/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Dirty - foo - done"));
    }

    @Test
    public void testResourceTremendous() {
        given().get("/api/resource/tremendous/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Tremendous - foo - done"));
    }

    @Test
    public void testResourcePrecious() {
        given().get("/api/resource/precious/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Precious - foo - done"));
    }

    @Test
    public void testResourceHunky() {
        given().get("/api/resource/hunky/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Hunky - foo - done"));
    }

    @Test
    public void testResourceFalse() {
        given().get("/api/resource/false/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("False - foo - done"));
    }

    @Test
    public void testResourceSmooth() {
        given().get("/api/resource/smooth/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Smooth - foo - done"));
    }

    @Test
    public void testResourceNaughty() {
        given().get("/api/resource/naughty/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Naughty - foo - done"));
    }

    @Test
    public void testResourceSteep() {
        given().get("/api/resource/steep/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Steep - foo - done"));
    }

    @Test
    public void testResourceOafish() {
        given().get("/api/resource/oafish/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Oafish - foo - done"));
    }

    @Test
    public void testResourceCertain() {
        given().get("/api/resource/certain/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Certain - foo - done"));
    }

    @Test
    public void testResourceWrathful() {
        given().get("/api/resource/wrathful/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Wrathful - foo - done"));
    }

    @Test
    public void testResourceClammy() {
        given().get("/api/resource/clammy/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Clammy - foo - done"));
    }

    @Test
    public void testResourceGrand() {
        given().get("/api/resource/grand/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Grand - foo - done"));
    }

    @Test
    public void testResourceFor() {
        given().get("/api/resource/for/foo").then().statusCode(HttpStatus.SC_OK).body("content", is("For - foo - done"));
    }

    @Test
    public void testResourceMindless() {
        given().get("/api/resource/mindless/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Mindless - foo - done"));
    }

    @Test
    public void testResourceAxiomatic() {
        given().get("/api/resource/axiomatic/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Axiomatic - foo - done"));
    }

    @Test
    public void testResourcePrimo() {
        given().get("/api/resource/primo/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Primo - foo - done"));
    }

    @Test
    public void testResourcePanoramic() {
        given().get("/api/resource/panoramic/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Panoramic - foo - done"));
    }

    @Test
    public void testResourceGlamorous() {
        given().get("/api/resource/glamorous/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Glamorous - foo - done"));
    }

    @Test
    public void testResourceStraight() {
        given().get("/api/resource/straight/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Straight - foo - done"));
    }

    @Test
    public void testResourceHushed() {
        given().get("/api/resource/hushed/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Hushed - foo - done"));
    }

    @Test
    public void testResourceSunny() {
        given().get("/api/resource/sunny/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Sunny - foo - done"));
    }

    @Test
    public void testResourceDisgusting() {
        given().get("/api/resource/disgusting/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Disgusting - foo - done"));
    }

    @Test
    public void testResourceFeigned() {
        given().get("/api/resource/feigned/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Feigned - foo - done"));
    }

    @Test
    public void testResourceInnocent() {
        given().get("/api/resource/innocent/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Innocent - foo - done"));
    }

    @Test
    public void testResourceBustling() {
        given().get("/api/resource/bustling/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Bustling - foo - done"));
    }

    @Test
    public void testResourceTame() {
        given().get("/api/resource/tame/foo").then().statusCode(HttpStatus.SC_OK).body("content", is("Tame - foo - done"));
    }

    @Test
    public void testResourceRecondite() {
        given().get("/api/resource/recondite/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Recondite - foo - done"));
    }

    @Test
    public void testResourceSqualid() {
        given().get("/api/resource/squalid/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Squalid - foo - done"));
    }

    @Test
    public void testResourceAdorable() {
        given().get("/api/resource/adorable/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Adorable - foo - done"));
    }

    @Test
    public void testResourceDistinct() {
        given().get("/api/resource/distinct/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Distinct - foo - done"));
    }

    @Test
    public void testResourceMany() {
        given().get("/api/resource/many/foo").then().statusCode(HttpStatus.SC_OK).body("content", is("Many - foo - done"));
    }

    @Test
    public void testResourceFlagrant() {
        given().get("/api/resource/flagrant/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Flagrant - foo - done"));
    }

    @Test
    public void testResourcePacific() {
        given().get("/api/resource/pacific/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Pacific - foo - done"));
    }

    @Test
    public void testResourcePossible() {
        given().get("/api/resource/possible/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Possible - foo - done"));
    }

    @Test
    public void testResourceRoof() {
        given().get("/api/resource/roof/foo").then().statusCode(HttpStatus.SC_OK).body("content", is("Roof - foo - done"));
    }
}
