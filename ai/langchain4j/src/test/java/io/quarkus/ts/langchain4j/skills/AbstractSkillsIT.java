package io.quarkus.ts.langchain4j.skills;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.RestService;
import io.restassured.response.Response;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractSkillsIT {

    protected static final String SKILLS_DIR = "src/test/resources/ai-skills";

    protected abstract String getFinalSkillsDir();

    protected abstract RestService getApp();

    @Test
    public void filepathSkillLoaded() {
        Response response = getApp().given().get("/skills/temperature/Brno");
        assertEquals(200, response.statusCode());
        String temperature = response.getBody().asString();
        assertEquals("12", temperature, "Wrong temperature, skill content may have been ignored!");
        List<String> logs = getApp().getLogs().stream().filter(line -> line.contains("Loaded 1 skill(s) from directory"))
                .toList();
        assertEquals(1, logs.size(), "Skill was not loaded or was loaded multiple times: " + logs);
        assertTrue(logs.get(0).contains(getFinalSkillsDir()));
    }

    @Test
    @Order(1)
    public void skillNotLoaded() {
        Response response = getApp().given().get("/skills/country/Brno");
        assertEquals(200, response.statusCode());
        List<String> logs = getApp().getLogs().stream().filter(line -> line.contains("Loaded 1 skill(s)")).toList();
        assertEquals(0, logs.size(), "Skill was loaded!" + logs);
    }

    @Test
    public void programmaticSkill() {
        Response response = getApp().given().body("What is the current version of this app?").post("/skills/info");
        assertEquals(200, response.statusCode());
        String answer = response.getBody().asString();
        assertTrue(answer.contains("1.0.0-SNAPSHOT"), "Wrong response, skill content may have been ignored: " + answer);
        assertTrue(answer.startsWith("My most dear colleague"),
                "Wrong response, resource content may have been ignored: " + answer);
    }

}
