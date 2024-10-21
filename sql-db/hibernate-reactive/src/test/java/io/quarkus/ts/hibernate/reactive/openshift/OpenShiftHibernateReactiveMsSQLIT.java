package io.quarkus.ts.hibernate.reactive.openshift;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.ts.hibernate.reactive.MsSQLDatabaseHibernateReactiveIT;

@OpenShiftScenario
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/2017")
@DisabledIfSystemProperty(named = "ts.ibm-z-p.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/2017.")
@Disabled("https://github.com/microsoft/mssql-docker/issues/769")
public class OpenShiftHibernateReactiveMsSQLIT extends MsSQLDatabaseHibernateReactiveIT {
}
