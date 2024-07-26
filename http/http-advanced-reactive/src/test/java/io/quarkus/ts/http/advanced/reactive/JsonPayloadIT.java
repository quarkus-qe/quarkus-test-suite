package io.quarkus.ts.http.advanced.reactive;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@QuarkusScenario
public class JsonPayloadIT {

    private static final int CREATION_STATUS_CODE = HttpStatus.SC_CREATED;
    private static final int OK_STATUS_CODE = HttpStatus.SC_OK;
    private static final int UNSUPPORTED_MEDIA_TYPE_STATUS_CODE = HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE;
    private static final int INTERNAL_SERVER_ERROR_STATUS_CODE = HttpStatus.SC_INTERNAL_SERVER_ERROR;

    @QuarkusApplication(classes = { FootballTeamResource.class, FootballTeam.class,
            Person.class, PersonResource.class, JsonExceptionMapper.class }, properties = "oidcdisable.properties")
    static RestService app = new RestService();

    @BeforeEach
    public void setup() {
        app.given()
                .when()
                .delete("/football/clear")
                .then()
                .statusCode(OK_STATUS_CODE);
    }

    @Test
    public void sendSuccessfulJSONPayloadFromAFile() throws IOException {
        String jsonPayload = readJSONFile("football-teams.json");
        app.given()
                .contentType(ContentType.JSON)
                .body(jsonPayload)
                .when()
                .post("/football/upload-football-json")
                .then()
                .statusCode(CREATION_STATUS_CODE)
                .body("teams.size()", is(26))
                .body(containsString("Teams added successfully"));

    }

    @Test
    public void sendLargeComplexJSONPayload() throws IOException {
        String jsonPayload = readJSONFile("big_sample.json");
        app.given()
                .contentType(ContentType.JSON)
                .body(jsonPayload)
                .when()
                .post("/persons/process-json")
                .then()
                .statusCode(CREATION_STATUS_CODE)
                .body(containsString("619 Rockaway Parkway, Hillsboro, California, 7507"));

        app.given()
                .get("/persons")
                .then()
                .statusCode(OK_STATUS_CODE)
                .body(containsString("Hello, Singleton Middleton! You have 4 unread messages."));

    }

    @Test
    public void sendJSONWithEmptyFieldsPayloadNoResourceValidation() throws IOException {
        String jsonPayload = readJSONFile("football-empty-teams.json");
        Response response = app.given()
                .contentType(ContentType.JSON)
                .body(jsonPayload)
                .when()
                .post("/football/upload-football-json")
                .then()
                .statusCode(CREATION_STATUS_CODE).extract().response();
        JsonPath jsonPath = response.jsonPath();
        // Check that there are 2 teams with empty colorTShirt fields
        assertThat(jsonPath.getList("teams.findAll { it.colorTShirt == '' }"), hasSize(2));
    }

    @Test
    public void sendInvalidJSONPayload() throws IOException {
        String jsonPayload = readJSONFile("football-teams_invalid.json");
        app.given()
                .contentType(ContentType.JSON)
                .body(jsonPayload)
                .when()
                .post("/football/upload-football-json")
                .then()
                .statusCode(INTERNAL_SERVER_ERROR_STATUS_CODE)
                .body(containsString("Internal error: Invalid token=CURLYOPEN"),
                        containsString("Expected tokens are: [COMMA]"));
    }

    @Test
    public void sendASingleJSONObject() {
        FootballTeam teamX = new FootballTeam(
                "Single Team",
                "Pink",
                3,
                LocalDate.of(1900, 1, 1),
                "A",
                Arrays.asList("You", "You"));

        app.given()
                .contentType(ContentType.JSON)
                .body(teamX)
                .when()
                .post("/football/upload-football-json")
                .then()
                .statusCode(INTERNAL_SERVER_ERROR_STATUS_CODE)
                .body(containsString(
                        "Incorrect position for processing type: interface java.util.List. Received event: START_OBJECT Allowed: [START_ARRAY]"));

    }

    @Test
    public void sendIncorrectContentType() throws IOException {
        String jsonPayload = readJSONFile("football-empty-teams.json");
        app.given()
                .contentType(ContentType.TEXT)
                .body(jsonPayload)
                .when()
                .post("/football/upload-football-json")
                .then()
                .statusCode(UNSUPPORTED_MEDIA_TYPE_STATUS_CODE);

    }

    @Test
    public void sendSpecialCharactersJSONPayload() throws Exception {
        String jsonPayload = readJSONFile("footbal-teams_specialchars.json");
        Response response = app.given()
                .contentType(ContentType.JSON)
                .body(jsonPayload)
                .when()
                .post("/football/upload-football-json")
                .then()
                .statusCode(CREATION_STATUS_CODE)
                .body(containsString("Teams added successfully")).extract().response();

        JsonPath jsonPath = response.jsonPath();
        List<?> teams = jsonPath.getList("teams");

        assertThat(teams, hasSize(2));
        assertThat(jsonPath.getString("message"), is("Teams added successfully"));

        assertThat(jsonPath.getString("teams[0].name"), containsString("Føøbåll™ Tëâm"));
        assertThat(jsonPath.getString("teams[0].keyPlayers[0]"), is("لاعب Ône"));
        assertThat(jsonPath.getString("teams[0].keyPlayers[1]"), is("Pläÿér Twø´ñ"));

        assertThat(jsonPath.getString("teams[1].name"), containsString("Tëâm 2"));
        assertThat(jsonPath.getString("teams[1].keyPlayers[0]"), is("Pläÿér プレイ"));
        assertThat(jsonPath.getString("teams[1].keyPlayers[1]"), is("Pläÿér Fòur"));
    }

    @Test
    public void sendJSONPayLoadWithNullIntegerField() throws IOException {
        String jsonPayload = readJSONFile("football-teams_nulls.json");
        app.given()
                .contentType(ContentType.JSON)
                .body(jsonPayload)
                .when()
                .post("/football/upload-football-json")
                .then()
                .statusCode(INTERNAL_SERVER_ERROR_STATUS_CODE)
                .body(containsString("Unable to deserialize property 'euroCups' "));
    }

    @Test
    public void sendJSONPayLoadWithNullFieldWillPass() {

        FootballTeam team1 = new FootballTeam(null, "Red", 3, LocalDate.of(1909, 10, 26), "Santiago Bernabéu",
                List.of("Sergio Ramos", "Andrés Iniesta"));
        List<FootballTeam> teams = List.of(team1);

        Jsonb jsonb = JsonbBuilder.create();
        String jsonString = jsonb.toJson(teams);

        app.given()
                .contentType(ContentType.JSON)
                .body(jsonString)
                .when()
                .post("/football/upload-football-json")
                .then()
                .statusCode(CREATION_STATUS_CODE)
                .body("teams", everyItem(not(hasKey("name"))));

    }

    private String readJSONFile(String jsonFile) throws IOException {
        return FileUtils.readFileToString(
                Paths.get("src", "test", "resources", jsonFile).toFile(),
                StandardCharsets.UTF_8);
    }
}
