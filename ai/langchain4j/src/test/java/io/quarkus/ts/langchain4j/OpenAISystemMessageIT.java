package io.quarkus.ts.langchain4j;

import static io.quarkus.ts.langchain4j.auxiliary.CommonTools.getKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

@QuarkusScenario
public class OpenAISystemMessageIT {

    @QuarkusApplication()
    static final RestService app = new RestService().withProperty("quarkus.profile", "openai")
            .withProperty("quarkus.langchain4j.openai.api-key", getKey());

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
    }
}
