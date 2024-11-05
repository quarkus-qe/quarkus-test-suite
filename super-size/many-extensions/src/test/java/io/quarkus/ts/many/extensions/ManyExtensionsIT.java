package io.quarkus.ts.many.extensions;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnFipsAndNative;
import io.restassured.specification.RequestSpecification;

@DisabledOnFipsAndNative(reason = "QUARKUS-5233")
@QuarkusScenario
public class ManyExtensionsIT {

    private RequestSpecification HTTP_CLIENT_SPEC = given();

    @Test
    public void httpServer() {
        givenSpec().get("/api/hello").then().statusCode(HttpStatus.SC_OK).body("content", is("Hello, World!"));
    }

    @Test
    public void testResourceShowy() {
        givenSpec().get("/api/resource/showy/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Showy - foo - done"));
    }

    @Test
    public void testResourceTricky() {
        givenSpec().get("/api/resource/tricky/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Tricky - foo - done"));
    }

    @Test
    public void testResourceObsolete() {
        givenSpec().get("/api/resource/obsolete/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Obsolete - foo - done"));
    }

    @Test
    public void testResourcePassenger() {
        givenSpec().get("/api/resource/passenger/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Passenger - foo - done"));
    }

    @Test
    public void testResourceDisastrous() {
        givenSpec().get("/api/resource/disastrous/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Disastrous - foo - done"));
    }

    @Test
    public void testResourceZany() {
        givenSpec().get("/api/resource/zany/foo").then().statusCode(HttpStatus.SC_OK).body("content", is("Zany - foo - done"));
    }

    @Test
    public void testResourceSlim() {
        givenSpec().get("/api/resource/slim/foo").then().statusCode(HttpStatus.SC_OK).body("content", is("Slim - foo - done"));
    }

    @Test
    public void testResourcePeachy() {
        givenSpec().get("/api/resource/peachy/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Peachy - foo - done"));
    }

    @Test
    public void testResourceAmused() {
        givenSpec().get("/api/resource/amused/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Amused - foo - done"));
    }

    @Test
    public void testResourceUnnatural() {
        givenSpec().get("/api/resource/unnatural/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Unnatural - foo - done"));
    }

    @Test
    public void testResourceHelpful() {
        givenSpec().get("/api/resource/helpful/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Helpful - foo - done"));
    }

    @Test
    public void testResourceAmbitious() {
        givenSpec().get("/api/resource/ambitious/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Ambitious - foo - done"));
    }

    @Test
    public void testResourceLarge() {
        givenSpec().get("/api/resource/large/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Large - foo - done"));
    }

    @Test
    public void testResourceMean() {
        givenSpec().get("/api/resource/mean/foo").then().statusCode(HttpStatus.SC_OK).body("content", is("Mean - foo - done"));
    }

    @Test
    public void testResourceShaggy() {
        givenSpec().get("/api/resource/shaggy/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Shaggy - foo - done"));
    }

    @Test
    public void testResourceOpposite() {
        givenSpec().get("/api/resource/opposite/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Opposite - foo - done"));
    }

    @Test
    public void testResourceGentle() {
        givenSpec().get("/api/resource/gentle/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Gentle - foo - done"));
    }

    @Test
    public void testResourceSmelly() {
        givenSpec().get("/api/resource/smelly/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Smelly - foo - done"));
    }

    @Test
    public void testResourceAquatic() {
        givenSpec().get("/api/resource/aquatic/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Aquatic - foo - done"));
    }

    @Test
    public void testResourceSteady() {
        givenSpec().get("/api/resource/steady/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Steady - foo - done"));
    }

    @Test
    public void testResourceRelieved() {
        givenSpec().get("/api/resource/relieved/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Relieved - foo - done"));
    }

    @Test
    public void testResourceGroovy() {
        givenSpec().get("/api/resource/groovy/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Groovy - foo - done"));
    }

    @Test
    public void testResourceSable() {
        givenSpec().get("/api/resource/sable/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Sable - foo - done"));
    }

    @Test
    public void testResourceDirty() {
        givenSpec().get("/api/resource/dirty/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Dirty - foo - done"));
    }

    @Test
    public void testResourceTremendous() {
        givenSpec().get("/api/resource/tremendous/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Tremendous - foo - done"));
    }

    @Test
    public void testResourcePrecious() {
        givenSpec().get("/api/resource/precious/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Precious - foo - done"));
    }

    @Test
    public void testResourceHunky() {
        givenSpec().get("/api/resource/hunky/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Hunky - foo - done"));
    }

    @Test
    public void testResourceFalse() {
        givenSpec().get("/api/resource/false/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("False - foo - done"));
    }

    @Test
    public void testResourceSmooth() {
        givenSpec().get("/api/resource/smooth/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Smooth - foo - done"));
    }

    @Test
    public void testResourceNaughty() {
        givenSpec().get("/api/resource/naughty/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Naughty - foo - done"));
    }

    @Test
    public void testResourceSteep() {
        givenSpec().get("/api/resource/steep/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Steep - foo - done"));
    }

    @Test
    public void testResourceOafish() {
        givenSpec().get("/api/resource/oafish/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Oafish - foo - done"));
    }

    @Test
    public void testResourceCertain() {
        givenSpec().get("/api/resource/certain/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Certain - foo - done"));
    }

    @Test
    public void testResourceWrathful() {
        givenSpec().get("/api/resource/wrathful/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Wrathful - foo - done"));
    }

    @Test
    public void testResourceClammy() {
        givenSpec().get("/api/resource/clammy/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Clammy - foo - done"));
    }

    @Test
    public void testResourceGrand() {
        givenSpec().get("/api/resource/grand/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Grand - foo - done"));
    }

    @Test
    public void testResourceFor() {
        givenSpec().get("/api/resource/for/foo").then().statusCode(HttpStatus.SC_OK).body("content", is("For - foo - done"));
    }

    @Test
    public void testResourceMindless() {
        givenSpec().get("/api/resource/mindless/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Mindless - foo - done"));
    }

    @Test
    public void testResourceAxiomatic() {
        givenSpec().get("/api/resource/axiomatic/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Axiomatic - foo - done"));
    }

    @Test
    public void testResourcePrimo() {
        givenSpec().get("/api/resource/primo/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Primo - foo - done"));
    }

    @Test
    public void testResourcePanoramic() {
        givenSpec().get("/api/resource/panoramic/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Panoramic - foo - done"));
    }

    @Test
    public void testResourceGlamorous() {
        givenSpec().get("/api/resource/glamorous/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Glamorous - foo - done"));
    }

    @Test
    public void testResourceStraight() {
        givenSpec().get("/api/resource/straight/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Straight - foo - done"));
    }

    @Test
    public void testResourceHushed() {
        givenSpec().get("/api/resource/hushed/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Hushed - foo - done"));
    }

    @Test
    public void testResourceSunny() {
        givenSpec().get("/api/resource/sunny/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Sunny - foo - done"));
    }

    @Test
    public void testResourceDisgusting() {
        givenSpec().get("/api/resource/disgusting/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Disgusting - foo - done"));
    }

    @Test
    public void testResourceFeigned() {
        givenSpec().get("/api/resource/feigned/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Feigned - foo - done"));
    }

    @Test
    public void testResourceInnocent() {
        givenSpec().get("/api/resource/innocent/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Innocent - foo - done"));
    }

    @Test
    public void testResourceBustling() {
        givenSpec().get("/api/resource/bustling/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Bustling - foo - done"));
    }

    @Test
    public void testResourceTame() {
        givenSpec().get("/api/resource/tame/foo").then().statusCode(HttpStatus.SC_OK).body("content", is("Tame - foo - done"));
    }

    @Test
    public void testResourceRecondite() {
        givenSpec().get("/api/resource/recondite/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Recondite - foo - done"));
    }

    @Test
    public void testResourceSqualid() {
        givenSpec().get("/api/resource/squalid/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Squalid - foo - done"));
    }

    @Test
    public void testResourceAdorable() {
        givenSpec().get("/api/resource/adorable/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Adorable - foo - done"));
    }

    @Test
    public void testResourceDistinct() {
        givenSpec().get("/api/resource/distinct/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Distinct - foo - done"));
    }

    @Test
    public void testResourceMany() {
        givenSpec().get("/api/resource/many/foo").then().statusCode(HttpStatus.SC_OK).body("content", is("Many - foo - done"));
    }

    @Test
    public void testResourceFlagrant() {
        givenSpec().get("/api/resource/flagrant/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Flagrant - foo - done"));
    }

    @Test
    public void testResourcePacific() {
        givenSpec().get("/api/resource/pacific/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Pacific - foo - done"));
    }

    @Test
    public void testResourcePossible() {
        givenSpec().get("/api/resource/possible/foo").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Possible - foo - done"));
    }

    @Test
    public void testResourceRoof() {
        givenSpec().get("/api/resource/roof/foo").then().statusCode(HttpStatus.SC_OK).body("content", is("Roof - foo - done"));
    }

    protected RequestSpecification givenSpec() {
        return HTTP_CLIENT_SPEC;
    }
}
