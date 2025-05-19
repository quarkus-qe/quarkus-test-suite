package io.quarkus.ts.http.hibernate.validator;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.http.hibernate.validator.sources.GraalVMFeatureResource;
import io.restassured.http.ContentType;

/**
 * This test verifies that Quarkus Hibernate Validator extension does not validate data types declared on
 * 'org.graalvm.nativeimage.hosted.Feature' implementor. More specifically, the {@link GraalVMFeatureResource} method
 * has formal parameter annotated with the {@link jakarta.validation.Valid} annotation. If the data type of the annotated
 * parameter has a subclass (or implementor in case of an interface) declared on the GraalVM feature, this resulted
 * previously in a native build failure, because all the GraalVM features are meant to be built-time only and GraalVM
 * removes them from the native executable. However, it can't be reproducer so easily because GraalVM does its own
 * validation and detects if you declare a data type used somewhere else in the same module. That is why for convenience
 * we use the 'angus-activation' dependency, which GraalVM fails to detect. From this dependency, we only care about
 * the 'AngusActivationFeature' class.
 */
@Tag("https://github.com/quarkusio/quarkus/issues/47033")
@QuarkusScenario
public class HibernateValidatorGraalVMFeatureValidationIT {

    @QuarkusApplication(classes = GraalVMFeatureResource.class, dependencies = {
            @Dependency(artifactId = "quarkus-rest-jackson"),
            // don't change 'angus-activation' version as when https://github.com/eclipse-ee4j/angus-activation/pull/52
            // is merged, following releases will not reproduce the issue
            @Dependency(groupId = "org.eclipse.angus", artifactId = "angus-activation", version = "2.0.2")
    }, properties = "graalvm-feature-validation.properties")
    static final RestService app = new RestService();

    @Test
    public void testHibernateValidationWithGraalVMFeature() {
        app
                .given()
                .contentType(ContentType.JSON)
                .body(List.of("one", "two", "three", "four", "five"))
                .post("/graal-vm-feature")
                .then()
                .statusCode(200)
                .body(Matchers.is("Number 5 is alive"));
        app
                .given()
                .contentType(ContentType.JSON)
                .body(List.of("one", "two", "three", "four"))
                .post("/graal-vm-feature")
                .then()
                .statusCode(400)
                .body(Matchers.not(Matchers.containsString("is alive")))
                .body(Matchers.containsString("size must be between 5 and 5"));
    }
}
