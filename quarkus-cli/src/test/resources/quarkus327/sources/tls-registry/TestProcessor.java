package org.acme;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Produce;
import io.quarkus.deployment.builditem.ServiceStartBuildItem;
import io.quarkus.tls.TlsRegistryBuildItem;

public class TestProcessor {

    @Produce(ServiceStartBuildItem.class) // step is invoked if it produces or records something
    @BuildStep
    void useTlsRegistry(TlsRegistryBuildItem buildItem) {
        // nothing to do
    }

}
