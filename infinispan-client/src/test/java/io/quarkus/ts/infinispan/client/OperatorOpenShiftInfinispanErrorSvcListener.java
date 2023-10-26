package io.quarkus.ts.infinispan.client;

import java.util.Set;

import io.quarkus.test.bootstrap.ServiceContext;
import io.quarkus.test.bootstrap.ServiceListener;
import io.vertx.core.impl.ConcurrentHashSet;

/**
 * Makes sure Infinispan cluster is deleted even when there is error and {@link org.junit.jupiter.api.AfterAll}
 * is not called.
 */
public class OperatorOpenShiftInfinispanErrorSvcListener implements ServiceListener {

    private static final Set<String> TEST_CLASS_CACHE = new ConcurrentHashSet<>();

    @Override
    public void onServiceError(ServiceContext service, Throwable throwable) {
        if (TEST_CLASS_CACHE.add(service.getScenarioContext().getRunningTestClassName())) {
            BaseOpenShiftInfinispanIT.deleteInfinispanCluster();
        }
    }
}
