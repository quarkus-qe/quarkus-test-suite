package io.quarkus.ts.http.restclient.reactive.fault.tolerance;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SpecificRetryService extends AbstractFaultToleranceService {
    private final AtomicInteger atomicInteger = new AtomicInteger(0);

    @Override
    public String performAction() {
        final int count = atomicInteger.getAndIncrement();
        if (count < 1) {
            throw new RuntimeException("Failure on attempt " + count);
        }
        return "OK";
    }
}
