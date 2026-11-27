package io.quarkus.ts.spring.data.di;

import static io.quarkus.ts.spring.data.di.ListInjectionBean.LIST_INJECTION_BEAN_ENABLED;
import static io.quarkus.ts.spring.data.di.ListInjectionBean.LIST_INJECTION_BEAN_ENABLED_VALUE;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnSemeruJdk;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-2095")
@QuarkusScenario
// TODO: enable when Semeru with condy bug fix is shipped, expected to land in Jan 2027
@DisabledOnSemeruJdk(reason = "OpenJ9 condy bug breaks Quarkus 4 app startup, see https://github.com/eclipse-openj9/openj9/issues/25187")
public class ListInjectionIT {

    @QuarkusApplication(classes = ListInjectionBean.class)
    static RestService app = new RestService().withProperty(LIST_INJECTION_BEAN_ENABLED, LIST_INJECTION_BEAN_ENABLED_VALUE);

    @Test
    public void appStartsWhenUsingFieldInjectionWithAllAnnotationOnList() {
        Assertions.assertTrue(app.isRunning());
    }
}
