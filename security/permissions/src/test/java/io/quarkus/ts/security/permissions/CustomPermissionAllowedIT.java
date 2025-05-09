package io.quarkus.ts.security.permissions;

import org.apache.http.HttpStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.quarkus.test.scenarios.QuarkusScenario;

@QuarkusScenario
public class CustomPermissionAllowedIT extends BasePermissionAllowedIT {

    public static final String ROOT_CUSTOM_ANNOTATION_PERMISSION_ALLOWED = "/custom-annotation-permission-allowed";
    public static final String ROOT_CLASS_CUSTOM_PERMISSION_ALLOWED = "/class-custom-permission-allowed";
    public static final String ROOT_COMBINED_PERMISSION_ALLOWED = "/combined-permission-allowed";

    @ParameterizedTest
    @CsvSource({ USER + "," + HttpStatus.SC_OK,
            CREATOR + "," + HttpStatus.SC_FORBIDDEN,
            SERVICE + "," + HttpStatus.SC_OK,
            ADMIN + "," + HttpStatus.SC_OK,
            UNKNOWN_USER + "," + HttpStatus.SC_UNAUTHORIZED })
    public void testClassAndMethodCustomPermissionAllowed(String user, int statusCode) {
        sendGetRequest(user, ROOT_CLASS_CUSTOM_PERMISSION_ALLOWED + "/additional-permission", statusCode);
    }

    @ParameterizedTest
    @CsvSource({ USER + "," + HttpStatus.SC_FORBIDDEN,
            CREATOR + "," + HttpStatus.SC_OK,
            SERVICE + "," + HttpStatus.SC_OK,
            ADMIN + "," + HttpStatus.SC_OK,
            UNKNOWN_USER + "," + HttpStatus.SC_UNAUTHORIZED })
    public void testClassCustomPermissionAllowed(String user, int statusCode) {
        sendGetRequest(user, ROOT_CLASS_CUSTOM_PERMISSION_ALLOWED + "/no-additional-permission", statusCode,
                "Permitted by class annotation");
    }

    @ParameterizedTest
    @CsvSource({ USER + "," + HttpStatus.SC_OK,
            CREATOR + "," + HttpStatus.SC_FORBIDDEN,
            SERVICE + "," + HttpStatus.SC_OK,
            ADMIN + "," + HttpStatus.SC_OK,
            UNKNOWN_USER + "," + HttpStatus.SC_UNAUTHORIZED })
    public void testClassAndMethodCustomPermissionAllowedInclusive(String user, int statusCode) {
        sendGetRequest(user, ROOT_CLASS_CUSTOM_PERMISSION_ALLOWED + "/additional-permission-inclusive", statusCode);
    }

    @ParameterizedTest
    @CsvSource({ USER + "," + HttpStatus.SC_FORBIDDEN,
            CREATOR + "," + HttpStatus.SC_OK,
            SERVICE + "," + HttpStatus.SC_FORBIDDEN,
            ADMIN + "," + HttpStatus.SC_OK,
            UNKNOWN_USER + "," + HttpStatus.SC_UNAUTHORIZED })
    public void testClassPermissionCustomAllowedInclusive(String user, int statusCode) {
        sendGetRequest(user, ROOT_CLASS_CUSTOM_PERMISSION_ALLOWED + "/no-additional-permission-inclusive", statusCode,
                "Permitted by class annotation");
    }

    @ParameterizedTest
    @CsvSource({ "/annotated-can-read-and-create," + HttpStatus.SC_FORBIDDEN,
            "/annotated-can-read-and-create-inclusive," + HttpStatus.SC_FORBIDDEN,
            "/annotated-can-read-minimal," + HttpStatus.SC_FORBIDDEN,
            "/annotated-can-read-minimal-inclusive," + HttpStatus.SC_FORBIDDEN })
    public void testUserCombinedPermission(String endpoint, int statusCode) {
        sendGetRequest(USER, ROOT_COMBINED_PERMISSION_ALLOWED + endpoint, statusCode);
    }

    @ParameterizedTest
    @CsvSource({ "/annotated-can-read-and-create," + HttpStatus.SC_FORBIDDEN,
            "/annotated-can-read-and-create-inclusive," + HttpStatus.SC_FORBIDDEN,
            "/annotated-can-read-minimal," + HttpStatus.SC_FORBIDDEN,
            "/annotated-can-read-minimal-inclusive," + HttpStatus.SC_FORBIDDEN })
    public void testCreatorCombinedPermission(String endpoint, int statusCode) {
        sendGetRequest(CREATOR, ROOT_COMBINED_PERMISSION_ALLOWED + endpoint, statusCode);
    }

    @ParameterizedTest
    @CsvSource({ "/annotated-can-read-and-create," + HttpStatus.SC_OK,
            "/annotated-can-read-and-create-inclusive," + HttpStatus.SC_FORBIDDEN,
            "/annotated-can-read-minimal," + HttpStatus.SC_OK,
            "/annotated-can-read-minimal-inclusive," + HttpStatus.SC_FORBIDDEN })
    public void testServiceCombinedPermission(String endpoint, int statusCode) {
        sendGetRequest(SERVICE, ROOT_COMBINED_PERMISSION_ALLOWED + endpoint, statusCode);
    }

    @ParameterizedTest
    @CsvSource({ "/annotated-can-read-and-create," + HttpStatus.SC_OK,
            "/annotated-can-read-and-create-inclusive," + HttpStatus.SC_OK,
            "/annotated-can-read-minimal," + HttpStatus.SC_OK,
            "/annotated-can-read-minimal-inclusive," + HttpStatus.SC_OK })
    public void testAdminCombinedPermission(String endpoint, int statusCode) {
        sendGetRequest(ADMIN, ROOT_COMBINED_PERMISSION_ALLOWED + endpoint, statusCode);
    }

    @Override
    protected String getRoot() {
        return ROOT_CUSTOM_ANNOTATION_PERMISSION_ALLOWED;
    }
}
