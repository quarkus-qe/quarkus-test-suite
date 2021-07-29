package io.quarkus.qe.scheduling.quartz;

import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;

@OpenShiftScenario
@DisabledOnNative(reason = "Due to the workaround added in mysql.properties, this is not working on Native")
public class OpenShiftBasicQuartzIT extends BasicMySqlQuartzIT {
}
