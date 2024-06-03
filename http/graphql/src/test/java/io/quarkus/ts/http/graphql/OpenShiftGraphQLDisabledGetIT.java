package io.quarkus.ts.http.graphql;

import org.junit.jupiter.api.Disabled;

import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario
@Disabled("https://github.com/quarkus-qe/quarkus-test-framework/issues/1149")
public class OpenShiftGraphQLDisabledGetIT extends GraphQLDisabledGetIT {
}
