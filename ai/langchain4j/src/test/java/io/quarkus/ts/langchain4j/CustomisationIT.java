package io.quarkus.ts.langchain4j;

import static io.quarkus.ts.langchain4j.auxiliary.CommonTools.getKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.langchain4j.customisation.NamedBot;
import io.quarkus.ts.langchain4j.customisation.NamedModelCustomizer;
import io.quarkus.ts.langchain4j.customisation.NamedResource;
import io.quarkus.ts.langchain4j.customisation.PlainBot;
import io.quarkus.ts.langchain4j.customisation.PlainBotResource;
import io.quarkus.ts.langchain4j.customisation.PlainModelCustomizer;
import io.quarkus.ts.langchain4j.customisation.PriorityModelCustomizer;
import io.quarkus.ts.langchain4j.customisation.StreamingModelCustomizer;
import io.restassured.response.Response;

@QuarkusScenario
public class CustomisationIT {

    @QuarkusApplication(includeAllClassesFromMain = true, classes = { StreamingModelCustomizer.class,
            PlainBot.class, PlainModelCustomizer.class, PlainBotResource.class,
            PriorityModelCustomizer.class })
    static final RestService app = new RestService()
            .withProperty("quarkus.profile", "openai")
            .withProperty("quarkus.langchain4j.openai.api-key", getKey());

    @QuarkusApplication(classes = {
            PlainBot.class, PlainModelCustomizer.class, PlainBotResource.class,
            PriorityModelCustomizer.class,
            NamedModelCustomizer.class, NamedBot.class, NamedResource.class })
    static final RestService singleConfig = new RestService()
            .withProperty("quarkus.profile", "named")
            .withProperty("quarkus.langchain4j.openai.api-key", getKey())
            .withProperty("quarkus.langchain4j.openai.custom.api-key", getKey());

    @Test
    public void customSystemMessage() {
        Response response = app.given().body("Who are you and what can you do?").post("/chat");
        assertEquals(200, response.statusCode());
        String answer = response.body().asString();
        String unified = answer.toLowerCase();
        assertTrue(unified.contains("sam"), "System message was ignored! \n" + answer);
        assertTrue(unified.contains("openai"), "Answer doesn't contain provider name! \n" + answer);
        assertTrue(unified.contains("constrained"), "System message about the model was not applied! \n" + answer);
        assertFalse(unified.contains("hallucinat"), "Temperature overload was not applied! \n" + answer);

        // check, that builder was customised as well
        app.logs().assertContains("Customizing streaming chat model");
        app.logs().assertContains("[X-QE-Header: custom value]");
        app.logs().assertContains("Response from the streaming OPEN_AI model");
    }

    @Test
    public void customisedPlainModel() {
        Response response = app.given().body("Write 'cells interlinked' and nothing else as a response").post("/plain-chat");
        assertEquals(200, response.statusCode());
        String answer = response.body().asString().toLowerCase();
        assertTrue(answer.contains("cells interlinked"), "Unexpected answer from the model: " + answer);

        app.logs().assertContains("Customizing chat model");
        app.logs().assertContains("[X-QE-Header: another custom value]");
        app.logs().assertContains("Response from OPEN_AI model");

        // Priority customizer should run, but the header must be overwritten by a customizer with lower priority
        app.logs().assertContains("Customizing priority chat model");
        app.logs().assertContains("\"frequency_penalty\" : 0.5");
        app.logs().assertDoesNotContain("[X-QE-Header: first custom value]");
    }

    @Test
    public void namedOverload() {
        Response response = singleConfig.given().body("Write 'cells interlinked' and nothing else as a response")
                .post("/named");
        assertEquals(200, response.statusCode());
        String answer = response.body().asString().toLowerCase();
        assertTrue(answer.contains("cells interlinked"), "Unexpected answer from the model: " + answer);

        // named customisation should be applied
        singleConfig.logs().assertContains("Customizing single chat model");
        singleConfig.logs().assertContains("[X-QE-Header: only custom value]");

        // no other customisations should run
        singleConfig.logs().assertDoesNotContain("Customizing chat model");
        singleConfig.logs().assertDoesNotContain("[X-QE-Header: another custom value]");
        singleConfig.logs().assertDoesNotContain("Response from OPEN_AI model");
        singleConfig.logs().assertDoesNotContain("Customizing priority chat model");
        singleConfig.logs().assertContains("\"frequency_penalty\" : 0.0");
        singleConfig.logs().assertDoesNotContain("[X-QE-Header: first custom value]");
    }
}
