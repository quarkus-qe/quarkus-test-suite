package io.quarkus.ts.openshift.security.basic;

import org.apache.http.HttpStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.quarkus.test.scenarios.QuarkusScenario;

@QuarkusScenario
public class PermissionAllowedIT extends BasePermissionAllowedIT {

    public static final String ROOT_CLASS_PERMISSION_ALLOWED = "/class-permission-allowed";

    @Override
    protected String getRoot() {
        return "/default-permission-allowed";
    }

    @ParameterizedTest
    @CsvSource({ USER + "," + HttpStatus.SC_OK,
            CREATOR + "," + HttpStatus.SC_FORBIDDEN,
            SERVICE + "," + HttpStatus.SC_OK,
            ADMIN + "," + HttpStatus.SC_OK,
            UNKNOWN_USER + "," + HttpStatus.SC_UNAUTHORIZED })
    public void testClassAndMethodPermissionAllowed(String user, int statusCode) {
        sendGetRequest(user, ROOT_CLASS_PERMISSION_ALLOWED + "/additional-permission", statusCode);
    }

    @ParameterizedTest
    @CsvSource({ USER + "," + HttpStatus.SC_FORBIDDEN,
            CREATOR + "," + HttpStatus.SC_OK,
            SERVICE + "," + HttpStatus.SC_OK,
            ADMIN + "," + HttpStatus.SC_OK,
            UNKNOWN_USER + "," + HttpStatus.SC_UNAUTHORIZED })
    public void testClassPermissionAllowed(String user, int statusCode) {
        sendGetRequest(user, ROOT_CLASS_PERMISSION_ALLOWED + "/no-additional-permission", statusCode,
                "Permitted by class annotation");
    }

    @ParameterizedTest
    @CsvSource({ USER + "," + HttpStatus.SC_OK,
            CREATOR + "," + HttpStatus.SC_FORBIDDEN,
            SERVICE + "," + HttpStatus.SC_OK,
            ADMIN + "," + HttpStatus.SC_OK,
            UNKNOWN_USER + "," + HttpStatus.SC_UNAUTHORIZED })
    public void testClassAndMethodPermissionAllowedInclusive(String user, int statusCode) {
        sendGetRequest(user, ROOT_CLASS_PERMISSION_ALLOWED + "/additional-permission-inclusive", statusCode);
    }

    @ParameterizedTest
    @CsvSource({ USER + "," + HttpStatus.SC_FORBIDDEN,
            CREATOR + "," + HttpStatus.SC_OK,
            SERVICE + "," + HttpStatus.SC_FORBIDDEN,
            ADMIN + "," + HttpStatus.SC_OK,
            UNKNOWN_USER + "," + HttpStatus.SC_UNAUTHORIZED })
    public void testClassPermissionAllowedInclusive(String user, int statusCode) {
        sendGetRequest(user, ROOT_CLASS_PERMISSION_ALLOWED + "/no-additional-permission-inclusive", statusCode,
                "Permitted by class annotation");
    }
}
