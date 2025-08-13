package io.quarkus.ts.hibernate.reactive.openshift;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.ts.hibernate.reactive.MariaDbHibernateValidatorCallbackIT;

@OpenShiftScenario
@EnabledIfSystemProperty(named = "ts.redhat.registry.enabled", matches = "true")
public class OpenShiftMariaDbHibernateValidatorCallbackIT extends MariaDbHibernateValidatorCallbackIT {
}
