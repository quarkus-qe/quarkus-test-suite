package io.quarkus.ts.security.jpa.reactive;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;

@QuarkusScenario
public class PostgreSqlSecurityJpaReactiveIT extends AbstractSecurityJpaReactiveIT {

    @Container(image = "${postgresql.latest.image}", port = 5432, expectedLog = "listening on IPv4 address")
    // SCRAM-SHA-256 (Postgres) uses the password as an HMAC key; FIPS PKCS#11 (NSS) rejects short keys (CKR_KEY_SIZE_RANGE), so the short passwords fail
    // TODO: set the FIPS-safe (long) password in the Postgres DatabaseService of the test framework and drop this workaround
    static final PostgresqlService database = new PostgresqlService().withPassword("quarkus_test_fips_long_password");

}
