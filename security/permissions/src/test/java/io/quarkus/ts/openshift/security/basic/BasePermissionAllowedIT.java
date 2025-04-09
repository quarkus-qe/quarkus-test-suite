package io.quarkus.ts.openshift.security.basic;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

public abstract class BasePermissionAllowedIT {
    public static final String CUSTOM_PERMISSION_PATH = "/custom-permission";

    public static final String CUSTOM_PERMISSION_PARAMETER = "custom-permission";

    public static final String USER = "user";
    public static final String CREATOR = "creator";
    public static final String SERVICE = "service";
    public static final String ADMIN = "admin";
    public static final String UNKNOWN_USER = "unknown-user";

    @QuarkusApplication
    static RestService app = new RestService();

    @ParameterizedTest
    @CsvSource({ "/multiple-permission-annotation," + HttpStatus.SC_FORBIDDEN,
            "/one-permission-annotation," + HttpStatus.SC_OK,
            "/one-permission-annotation-inclusive," + HttpStatus.SC_FORBIDDEN,
            "/multiple-annotation-multiple-permission," + HttpStatus.SC_FORBIDDEN,
            "/multiple-annotation-multiple-permission-inclusive," + HttpStatus.SC_FORBIDDEN })
    public void testUser(String endpoint, int statusCode) {
        sendGetRequest(USER, getRoot() + endpoint, statusCode);
    }

    @ParameterizedTest
    @CsvSource({ "/multiple-permission-annotation," + HttpStatus.SC_OK,
            "/one-permission-annotation," + HttpStatus.SC_OK,
            "/one-permission-annotation-inclusive," + HttpStatus.SC_FORBIDDEN,
            "/multiple-annotation-multiple-permission," + HttpStatus.SC_FORBIDDEN,
            "/multiple-annotation-multiple-permission-inclusive," + HttpStatus.SC_FORBIDDEN })
    public void testCreator(String endpoint, int statusCode) {
        sendGetRequest(CREATOR, getRoot() + endpoint, statusCode);
    }

    @ParameterizedTest
    @CsvSource({ "/multiple-permission-annotation," + HttpStatus.SC_FORBIDDEN,
            "/one-permission-annotation," + HttpStatus.SC_OK,
            "/one-permission-annotation-inclusive," + HttpStatus.SC_OK,
            "/multiple-annotation-multiple-permission," + HttpStatus.SC_OK,
            "/multiple-annotation-multiple-permission-inclusive," + HttpStatus.SC_FORBIDDEN })
    public void testService(String endpoint, int statusCode) {
        sendGetRequest(SERVICE, getRoot() + endpoint, statusCode);
    }

    @ParameterizedTest
    @CsvSource({ "/multiple-permission-annotation," + HttpStatus.SC_OK,
            "/one-permission-annotation," + HttpStatus.SC_OK,
            "/one-permission-annotation-inclusive," + HttpStatus.SC_OK,
            "/multiple-annotation-multiple-permission," + HttpStatus.SC_OK,
            "/multiple-annotation-multiple-permission-inclusive," + HttpStatus.SC_OK })
    public void testAdmin(String endpoint, int statusCode) {
        sendGetRequest(ADMIN, getRoot() + endpoint, statusCode);
    }

    @ParameterizedTest
    @CsvSource({ "/multiple-permission-annotation," + HttpStatus.SC_UNAUTHORIZED,
            "/one-permission-annotation," + HttpStatus.SC_UNAUTHORIZED,
            "/one-permission-annotation-inclusive," + HttpStatus.SC_UNAUTHORIZED,
            "/multiple-annotation-multiple-permission," + HttpStatus.SC_UNAUTHORIZED,
            "/multiple-annotation-multiple-permission-inclusive," + HttpStatus.SC_UNAUTHORIZED })
    public void testUnknownUser(String endpoint, int statusCode) {
        sendGetRequest(UNKNOWN_USER, getRoot() + endpoint, statusCode);
    }

    @ParameterizedTest
    @CsvSource({ USER + "," + HttpStatus.SC_OK,
            CREATOR + "," + HttpStatus.SC_OK,
            SERVICE + "," + HttpStatus.SC_OK,
            ADMIN + "," + HttpStatus.SC_OK,
            UNKNOWN_USER + "," + HttpStatus.SC_UNAUTHORIZED })
    public void testCustomPermission(String user, int statusCode) {
        sendGetRequestWithParameter(user, CUSTOM_PERMISSION_PATH + "/custom-permission", statusCode,
                CUSTOM_PERMISSION_PARAMETER);
    }

    @ParameterizedTest
    @CsvSource({ USER + "," + HttpStatus.SC_FORBIDDEN,
            CREATOR + "," + HttpStatus.SC_FORBIDDEN,
            SERVICE + "," + HttpStatus.SC_OK,
            ADMIN + "," + HttpStatus.SC_OK,
            UNKNOWN_USER + "," + HttpStatus.SC_UNAUTHORIZED })
    public void testCustomPermissionInclusive(String user, int statusCode) {
        sendGetRequestWithParameter(user, CUSTOM_PERMISSION_PATH + "/custom-permission-inclusive", statusCode,
                CUSTOM_PERMISSION_PARAMETER);
    }

    @ParameterizedTest
    @CsvSource({ USER + "," + HttpStatus.SC_FORBIDDEN,
            CREATOR + "," + HttpStatus.SC_FORBIDDEN,
            SERVICE + "," + HttpStatus.SC_FORBIDDEN,
            ADMIN + "," + HttpStatus.SC_FORBIDDEN,
            UNKNOWN_USER + "," + HttpStatus.SC_UNAUTHORIZED })
    public void testCustomPermissionWrongParameter(String user, int statusCode) {
        sendGetRequestWithParameter(user, CUSTOM_PERMISSION_PATH + "/custom-permission", statusCode, "unknown");
    }

    public void sendGetRequest(String user, String endpoint, int statusCode) {
        sendGetRequest(user, endpoint, statusCode, "Permitted");
    }

    public void sendGetRequest(String user, String endpoint, int statusCode, String expectedResponseText) {
        Response response = app.given()
                .auth().basic(user, user)
                .get(endpoint);
        assertEquals(statusCode, response.getStatusCode());
        if (statusCode == HttpStatus.SC_OK) {
            assertEquals(expectedResponseText, response.body().asString());
        }
    }

    public void sendGetRequestWithParameter(String user, String endpoint, int statusCode, String parameter) {
        Response response = app.given()
                .auth().basic(user, user)
                .param(parameter, parameter)
                .get(getRoot() + endpoint);
        assertEquals(statusCode, response.getStatusCode());
        if (statusCode == HttpStatus.SC_OK) {
            assertEquals("Permitted query parameter: " + parameter, response.body().asString());
        }
    }

    protected abstract String getRoot();
}
