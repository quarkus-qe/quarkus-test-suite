package io.quarkus.ts.openshift.security.basic.beanparam;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.specification.RequestSpecification;

@QuarkusScenario
public abstract class BaseBeanParamPermissionsIT {

    public static final String USER = "user";
    public static final String ADMIN = "admin";

    public static final String SIMPLE_ENDPOINT = "/bean-param/simple";
    public static final String RECORD_ENDPOINT = "/bean-param/record";
    public static final String COMMON_FIELD_ENDPOINT = "/bean-param/common-field";
    public static final String NESTED_DOCUMENT_ENDPOINT = "/bean-param/nested-structure/document";
    public static final String NESTED_PROFILE_ENDPOINT = "/bean-param/nested-structure/profile";

    protected abstract RequestSpecification givenAuthenticatedUser(String role);

}
