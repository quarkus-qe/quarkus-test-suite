package io.quarkus.ts.hibernate.reactive;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.ts.hibernate.reactive.database.Fruit;
import io.quarkus.ts.hibernate.reactive.database.XmlValidatedCustomer;

@QuarkusScenario
@Tag("QUARKUS-6792")
@Tag("podman-incompatible") //TODO: https://github.com/quarkusio/quarkus/issues/38003
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkusio/quarkus/issues/43375")
public class DevModeSchemaValidationIT {
    // Add only Fruit class, since that one is used for issue reproduction.
    // Other entities will only require handling their tables in DB and since we create those manually in this reproducer,
    // we would need to manually handle all of them.
    // We need to keep XmlValidatedCustomer because of hibernate validator, otherwise the app will not start
    @DevModeQuarkusApplication(classes = { Fruit.class, XmlValidatedCustomer.class })
    static RestService app = new RestService().withProperties("schema_validation.properties");

    @Test
    public void validate() {
        // Validate issue https://github.com/hibernate/hibernate-reactive/issues/2738
        // Hibernate validation would fail to recognize SQL column type "nvarchar2"
        app.logs().assertDoesNotContain(
                "wrong column type encountered in column [something_name] in table [Fruit]; found [nvarchar2");
    }
}
