package io.quarkus.ts.http.restclient.reactive.fault.tolerance;

import org.eclipse.microprofile.faulttolerance.Retry;

@Retry(maxRetries = 2)
public abstract class AbstractFaultToleranceService {
    public abstract String performAction();
}
