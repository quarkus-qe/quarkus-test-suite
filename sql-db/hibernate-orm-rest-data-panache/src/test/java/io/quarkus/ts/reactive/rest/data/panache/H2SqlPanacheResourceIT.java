package io.quarkus.ts.reactive.rest.data.panache;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.scenarios.annotations.DisabledOnSemeruJdk;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@DisabledOnNative(reason = "H2 database compiled into a native-image is only functional as a client")
// TODO: enable when Semeru with condy bug fix is shipped, expected to land in Jan 2027
@DisabledOnSemeruJdk(reason = "OpenJ9 condy bug breaks Quarkus 4 app startup, see https://github.com/eclipse-openj9/openj9/issues/25187")
public class H2SqlPanacheResourceIT extends AbstractPanacheResourceIT {

    @QuarkusApplication
    static RestService app = new RestService().withProperties("h2.properties");
}
