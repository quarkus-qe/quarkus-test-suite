package io.quarkus.ts.hibernate.reactive.openshift;

import org.junit.jupiter.api.Disabled;

import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.ts.hibernate.reactive.OracleDatabaseIT;

@Disabled("https://issues.redhat.com/browse/QUARKUS-1164?focusedCommentId=18977100&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-18977100")
@OpenShiftScenario
public class OpenShiftOracleIT extends OracleDatabaseIT {
}
