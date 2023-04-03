package io.quarkus.ts.configmap.api.server;

import static org.hamcrest.Matchers.is;

import java.util.function.Supplier;

import io.restassured.specification.RequestSpecification;

class PropertiesSource {
    static final String APP_PROPERTIES = "app-prop";
    static final PropertiesSource INJECTED_PROPERTIES = new PropertiesSource("properties", true, null);
    static final PropertiesSource BUILT_CONFIG = new PropertiesSource("built", true, null);
    static final PropertiesSource INJECTED_CONFIG = new PropertiesSource("injected", true, null);
    static Supplier<RequestSpecification> given;

    private final String resource;
    private final boolean locked;
    private final String secretKeyPrefix;

    PropertiesSource(String resource, boolean locked, String secretKeyPrefix) {
        this.resource = resource;
        this.locked = locked;
        this.secretKeyPrefix = secretKeyPrefix;
    }

    PropertiesSource unlocked() {
        return new PropertiesSource(this.resource, false, this.secretKeyPrefix);
    }

    void assertSecret(String secretKey, String expectedSecretValue) {
        given()
                .pathParam("resource", resource)
                .pathParam("endpoint", locked ? "locked" : "unlocked")
                .pathParam("secretKey", keyPrefix() + secretKey)
                .get("/{resource}/{endpoint}/{secretKey}")
                .then()
                .statusCode(200)
                .body(is(expectedSecretValue));
    }

    void assertAccessDenied(String secretKey) {
        given()
                .pathParam("resource", resource)
                .pathParam("endpoint", locked ? "locked" : "unlocked")
                .pathParam("secretKey", keyPrefix() + secretKey)
                .get("/{resource}/{endpoint}/{secretKey}")
                .then()
                .statusCode(500)
                .body(is("SRCFG00024: Not allowed to access secret key " + secretKey));
    }

    private String keyPrefix() {
        return secretKeyPrefix == null ? "" : secretKeyPrefix;
    }

    private static RequestSpecification given() {
        return given.get();
    }
}
