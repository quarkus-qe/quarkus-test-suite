package io.quarkus.ts.http.advanced.reactive;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@QuarkusScenario
public abstract class JsonPayloadIT {

    protected abstract RestService getApp();

    private static final int CREATION_STATUS_CODE = HttpStatus.SC_CREATED;
    private static final int BAD_REQUEST_STATUS_CODE = HttpStatus.SC_BAD_REQUEST;
    private static final int OK_STATUS_CODE = HttpStatus.SC_OK;
    private static final int UNSUPPORTED_MEDIA_TYPE_STATUS_CODE = HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE;

    @BeforeEach
    public void setup() {
        getApp().given()
                .when()
                .delete("/football/clear")
                .then()
                .statusCode(OK_STATUS_CODE);
    }

    @Test
    public void sendSuccessfulJSONPayloadFromAFile() throws IOException {

        String jsonPayload = readJSONFile("football-teams.json");

        Response response = getApp().given()
                .contentType(ContentType.JSON)
                .body(jsonPayload)
                .when()
                .post("/football/upload-football-json")
                .then()
                .statusCode(CREATION_STATUS_CODE)
                .body("teams.size()", is(26))
                .extract().response();

        assertThat(response.body().asString(), containsString("Teams added successfully"));

    }

    @Test
    public void sendLargeComplexJSONPayload() throws IOException {
        String jsonPayload = readJSONFile("big_sample.json");

        getApp().given()
                .contentType(ContentType.JSON)
                .body(jsonPayload)
                .when()
                .post("/persons/process-json")
                .then()
                .statusCode(CREATION_STATUS_CODE)
                .body(containsString("619 Rockaway Parkway, Hillsboro, California, 7507"));

        getApp().given()
                .get("/persons")
                .then()
                .statusCode(OK_STATUS_CODE)
                .body(containsString("Hello, Singleton Middleton! You have 4 unread messages."));

    }

    @Test
    public void sendJSONWithEmptyFieldsPayload() throws IOException {
        String jsonPayload = readJSONFile("football-empty-teams.json");

        getApp().given()
                .contentType(ContentType.JSON)
                .body(jsonPayload)
                .when()
                .post("/football/upload-football-json")
                .then()
                .statusCode(BAD_REQUEST_STATUS_CODE)
                .body(containsString("Some fields are empty or null"));
    }

    @Test
    public void sendInvalidJSONPayload() throws IOException {
        String jsonPayload = readJSONFile("football-teams_invalid.json");

        getApp().given()
                .contentType(ContentType.JSON)
                .body(jsonPayload)
                .when()
                .post("/football/upload-football-json")
                .then()
                .statusCode(BAD_REQUEST_STATUS_CODE)
                .body(containsString(
                        "Invalid JSON - JsonProcessingException : Unexpected character ('{' (code 123)): was expecting comma to separate Array entries"));
    }

    @Test
    public void sendASingleJSONObject() throws JsonProcessingException {
        FootballTeam teamX = new FootballTeam(
                "Single Team",
                "Pink",
                3,
                new Date(1900, 1, 1),
                "A",
                Arrays.asList("You", "You"));
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload = objectMapper.writeValueAsString(teamX);

        getApp().given()
                .contentType(ContentType.JSON)
                .body(jsonPayload)
                .when()
                .post("/football/upload-football-json")
                .then()
                .statusCode(BAD_REQUEST_STATUS_CODE)
                .body(containsString(
                        "JsonMappingException : Cannot deserialize value of type `java.util.HashSet<io.quarkus.ts.http.advanced.reactive.FootballTeam>`"));

    }

    @Test
    public void sendIncorrectContentType() throws IOException {
        String jsonPayload = readJSONFile("football-empty-teams.json");

        getApp().given()
                .contentType(ContentType.TEXT)
                .body(jsonPayload)
                .when()
                .post("/football/upload-football-json")
                .then()
                .statusCode(UNSUPPORTED_MEDIA_TYPE_STATUS_CODE);

    }

    @Test
    public void sendSpecialCharactersJSONPayload() throws Exception {
        FootballTeam team1 = new FootballTeam(
                "Føøbåll™ Tëâm",
                "Rëd&Blüé",
                3,
                new Date(1900, 1, 1),
                "Städiümß©",
                Arrays.asList("لاعب Ône", "Pläÿér Twø´ñ"));

        FootballTeam team2 = new FootballTeam(
                "Tëâm 2",
                "Grëèn&Yëllöw",
                2,
                new Date(2010, 1, 15),
                "New Städiüm©",
                Arrays.asList("Pläÿér プレイ", "Pläÿér Fòur"));

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload = objectMapper.writeValueAsString(Arrays.asList(team1, team2));

        getApp().given()
                .contentType(ContentType.JSON)
                .body(jsonPayload)
                .when()
                .post("/football/upload-football-json")
                .then()
                .statusCode(CREATION_STATUS_CODE)
                .body(containsString("Teams added successfully"));

    }

    private String readJSONFile(String jsonFile) throws IOException {
        return FileUtils.readFileToString(
                Paths.get("src", "test", "resources", jsonFile).toFile(),
                StandardCharsets.UTF_8);
    }
}
