package io.quarkus.ts.langchain4j.skills;

import static io.quarkus.ts.langchain4j.auxiliary.CommonTools.DEFAULT_ARGS;
import static io.quarkus.ts.langchain4j.auxiliary.CommonTools.SAMPLE_BRANCH;
import static io.quarkus.ts.langchain4j.auxiliary.CommonTools.getKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.GitRepositoryQuarkusApplication;
import io.restassured.response.Response;

@QuarkusScenario
@DisabledOnNative(reason = "https://github.com/quarkiverse/quarkus-langchain4j/issues/3044")
public class ClasspathSkillsIT {
    @GitRepositoryQuarkusApplication(repo = "https://github.com/quarkiverse/quarkus-langchain4j.git", branch = SAMPLE_BRANCH, contextDir = "samples/skills", mavenArgs = DEFAULT_ARGS
            + " -Dsamples -Dplatform-deps")
    static final RestService app = new RestService()
            .withProperty("quarkus.langchain4j.openai.api-key", getKey())
            .withProperty("quarkus.langchain4j.openai.chat-model.reasoning-effort", "none")
            .withProperty("quarkus.langchain4j.log-responses", "false")
            .withProperty("quarkus.langchain4j.log-requests", "false");

    @Test
    public void poem() {
        Response poem = app.given().get("/poem");
        assertEquals(200, poem.statusCode(), "Response is " + poem.body().asString());
        String body = poem.getBody().asString();
        int lines = body.split("\n").length;
        assertEquals(12, lines, "Wrong size of the poem, skill content may have been ignored!");
        assertTrue(body.contains("mushroom"), "The poem doesn't mention mushrooms, skill content may have been ignored!");
        app.logs().assertContains("Loaded 1 skill(s) from directory: classpath:skills");
    }

}
