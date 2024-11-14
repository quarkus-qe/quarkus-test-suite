package io.quarkus.ts.qute;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import jakarta.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

@QuarkusScenario
public class QuteReactiveIT {

    private static final String UTF8_HTML = MediaType.TEXT_HTML + ";charset=UTF-8";

    @QuarkusApplication
    static RestService app = new RestService();

    @Test
    void smoke() {
        Response response = app.given().get("/basic");
        assertEquals(200, response.statusCode());
        assertEquals(UTF8_HTML, response.contentType());

        final String result = response.body().asString();
        assertEquals(59, result.length());
        assertEquals("<html>\n    <p>This page is rendered by Quarkus</p>\n</html>\n", result);
    }

    @Test
    void location() {
        Response response = app.given().get("/location");
        assertEquals(200, response.statusCode());
        assertEquals(UTF8_HTML, response.contentType());
        assertTrue(response.body().asString().contains("<p>This page is fetched and rendered by Quarkus</p>"),
                response.body().asString());
    }

    @Test
    void engine() {
        Response response = app.given().get("/engine/basic");
        assertEquals(200, response.statusCode());
        assertEquals(UTF8_HTML, response.contentType());
        assertTrue(response.body().asString().contains("<p>This page is rendered by basic engine</p>"),
                response.body().asString());
    }

    @Test
    void register() {
        Response check = app.given().get("/engine/another");
        assertEquals(HttpStatus.SC_NOT_FOUND, check.statusCode());

        Response create = app.given().body("This page is rendered by {server} from HTTP request").put("/engine/another");
        assertEquals(HttpStatus.SC_CREATED, create.statusCode());

        Response fetch = app.given().get("/engine/another");
        assertEquals(UTF8_HTML, fetch.contentType());
        assertEquals("This page is rendered by another engine from HTTP request", fetch.body().asString());

        Response change = app.given().body("This page is rendered by {server} from HTTP request. Again")
                .put("/engine/another");
        assertEquals(HttpStatus.SC_NO_CONTENT, change.statusCode());

        Response fetchAgain = app.given().get("/engine/another");
        assertEquals("This page is rendered by another engine from HTTP request. Again", fetchAgain.body().asString());
    }

    @Test
    void searchAndDelete() {
        Response check = app.given().get("/engine/temporary");
        assertEquals(HttpStatus.SC_NOT_FOUND, check.statusCode());

        Response create = app.given().body("This page is rendered by {server} from POST HTTP request")
                .post("/engine/temporary");
        assertEquals(HttpStatus.SC_CREATED, create.statusCode());

        final String same = "This page is rendered by temporary engine from POST HTTP request";

        Response fetch = app.given().get("/engine/temporary");
        assertEquals(same, fetch.body().asString());

        Response createAgain = app.given().body("This page is rendered by {server} from HTTP request. Again")
                .post("/engine/temporary");
        assertEquals(HttpStatus.SC_CONFLICT, createAgain.statusCode());

        Response fetchAgain = app.given().get("/engine/temporary");
        assertEquals(same, fetchAgain.body().asString());

        assertEquals(HttpStatus.SC_OK, app.given().delete("/engine/temporary").statusCode());
        Response checkDeletion = app.given().get("/engine/temporary");
        assertEquals(HttpStatus.SC_NOT_FOUND, checkDeletion.statusCode());
    }

    @Test
    void format() {
        Response defaultType = app.given().get("/format?name=remote client");
        assertEquals(200, defaultType.statusCode());
        assertEquals(UTF8_HTML, defaultType.contentType());
        assertEquals("This page is rendered for \"remote client\" by Qute", defaultType.body().asString());

        Response html = app.given().accept("text/html").get("/format?name=html client");
        assertEquals(200, html.statusCode());
        assertEquals(UTF8_HTML, html.contentType());
        assertEquals("This page is rendered for \"html client\" by Qute", html.body().asString());

        Response plain = app.given().accept("text/plain").get("/format?name=plaintext client");
        assertEquals(200, plain.statusCode());
        assertEquals(MediaType.TEXT_PLAIN + ";charset=UTF-8", plain.contentType());
        assertEquals("This page is rendered for \"plaintext client\" by Qute", plain.body().asString());
    }

    @Test
    void advancedFormat() {
        Response response = app.given().get("/format-advanced?name=remote client");
        assertEquals(200, response.statusCode());
        assertEquals(UTF8_HTML, response.contentType());
        assertEquals("This text is fluently rendered for \"remote client\" by Qute", response.body().asString());
    }

    @Test
    void expressions() {
        Response response = app.given().get("/book");
        assertEquals(200, response.statusCode());
        assertEquals(UTF8_HTML, response.contentType());

        String[] content = response.body().asString().replace("<br>", "").split("\n");
        assertEquals(20, content.length);

        assertEquals("<html>", content[1]);
        assertEquals("<p>This page is rendered by engine</p>", content[2]);
        assertEquals("The book is called The Three Musketeers and is written by Alexandre Dumas",
                content[3]);

        final int loopStartLine = 5;
        assertEquals("It has 4 characters:", content[loopStartLine]);
        assertEquals("1. d'Artagnan as well as", content[loopStartLine + 2].replace("&#39;", "'"));
        assertEquals("2. Athos and also", content[loopStartLine + 3]);
        assertEquals("3. Porthos as well as", content[loopStartLine + 4]);
        assertEquals("4. Aramis", content[loopStartLine + 5]);

        assertEquals(
                "At first, d'Artagnan tries to fight on a duel with Athos, Porthos and Aramis, but later they become friends.",
                content[12].replace("&#39;", "'"));
        final int arrayStartLine = 13;
        assertEquals("Here are some numbers: 0 1 2 3 4", content[arrayStartLine]);
        assertEquals("I have to say, that this is odd", content[arrayStartLine + 1]);
        assertEquals("and this is even", content[arrayStartLine + 2]);
        assertEquals("and this is odd", content[arrayStartLine + 3]);
        assertEquals("and this is even", content[arrayStartLine + 4]);
        assertEquals("and this is odd.", content[arrayStartLine + 5]);
    }

    @Test
    void encoding() {
        Response response = app.given().get("/encoding");
        assertEquals(200, response.statusCode());
        assertEquals(UTF8_HTML, response.contentType());
        final String[] content = response.body().asString().split("\n");
        assertEquals("<p>Englishmen say hello</p>", content[2].stripLeading());
        assertEquals("<p>Češi říkají čau</p>", content[3].stripLeading());
        assertEquals("<p>Русские говорят привет</p>", content[4].stripLeading());
        assertEquals("<p dir=\"rtl\">יהודים היגדים שלום</p>", content[5].stripLeading());
        assertEquals("{česky}", content[6].stripLeading());
        assertEquals("{English}", content[7].stripLeading());
    }

    @Test
    void maps() {
        Response response = app.given().get("/map");
        assertEquals(200, response.statusCode());
        assertEquals(UTF8_HTML, response.contentType());
        final String[] content = response.body().asString().split("\n");
        assertEquals("The capital of Tasmania is Hobart, Jakarta is a capital of Java and London is a capital of the UK.",
                content[1].stripLeading());
        assertEquals("Yes, you're right, it's London!", content[3].stripLeading());
        int keysLine = 5;
        assertEquals("1. Java<br>", content[keysLine].stripLeading());
        assertEquals("2. Tasmania<br>", content[keysLine + 1].stripLeading());
        assertEquals("3. The Great Britain<br>", content[keysLine + 2].stripLeading());
        int valuesLine = 8;
        assertEquals("I am a Java programmer and there are 3 cities I know:<br>", content[valuesLine].stripLeading());
        assertEquals("0. Jakarta<br>", content[valuesLine + 1].stripLeading());
        assertEquals("1. Hobart<br>", content[valuesLine + 2].stripLeading());
        assertEquals("2. London<br>", content[valuesLine + 3].stripLeading());
    }

    @Test
    void emptyMaps() {
        Response response = app.given().get("/map?name=europe");
        assertEquals(200, response.statusCode());
        final String[] content = response.body().asString().split("\n");
        assertEquals("I am a Java programmer and there are zero cities I know:<br>", content[1].stripLeading());
    }

    @Test
    void inheritance() {
        Response base = app.given().get("/inheritance?name=base");
        assertEquals(200, base.statusCode());
        assertEquals(UTF8_HTML, base.contentType());
        final String[] baseContent = base.body().asString().split("\n");

        assertEquals(7, baseContent.length);
        assertEquals("<head><title>A poem</title></head>", baseContent[2].stripLeading());
        assertEquals("Empty body!", baseContent[4].stripLeading());

        Response inherited = app.given().get("/inheritance?name=detail");
        assertEquals(200, inherited.statusCode());
        assertEquals(UTF8_HTML, inherited.contentType());

        List<String> updatedContent = Stream.of(inherited.body().asString())
                .map(string -> string.split("\n"))
                .flatMap(Arrays::stream)
                .filter(string -> !string.isEmpty())
                .filter(string -> !string.isEmpty())
                .toList();
        assertEquals(9, updatedContent.size());
        assertEquals("<head><title>Auguries of Innocence</title></head>", updatedContent.get(2).stripLeading());
        assertEquals("Every Morn and every Night<br>", updatedContent.get(4).stripLeading());
        assertEquals("Some are Born to sweet delight<br>", updatedContent.get(5).stripLeading());
        assertEquals("Some are Born to sweet delight<br>", updatedContent.get(6).stripLeading());
        assertEquals("Some are Born to Endless Night<br></body>", updatedContent.get(7).stripLeading());
    }

    @Test
    void annotations() {
        Response response = app.given().get("/annotated");
        assertEquals(200, response.statusCode());
        final String[] content = response.body().asString().split("\n");
        assertEquals("Joe slaps Seligman around a bit with a large trout<br>", content[0].stripLeading());
        assertEquals("This trout stays silent", content[1].stripLeading());
        assertEquals("A quick br()wn f()x jumps ()ver the lazy d()g", content[2].stripLeading());
        assertEquals("Airspeed velocity of an unladen swallow is 11 m/s", content[3].stripLeading());
        assertEquals("This random number was chosen by a roll of dice: 5", content[4].stripLeading());
    }

    @Test
    void enums() {
        Response dublin = app.given().get("/enums/dublin");
        assertEquals(200, dublin.statusCode());
        assertEquals("Good news, we will spend a week in Dublin!", dublin.body().asString());

        Response bruges = app.given().get("/enums/bruges");
        assertEquals(200, bruges.statusCode());
        assertEquals("Good news, we will not spend a week in Bruges!", bruges.body().asString());
    }
}
