package io.quarkus.ts.opentelemetry.reactive;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.Scheduled.ApplicationNotRunning;

@ApplicationScoped
public class SchedulerService {

    private final AtomicInteger counter = new AtomicInteger();

    @Scheduled(every = "1s", skipExecutionIf = ApplicationNotRunning.class)
    void increment() {
        counter.incrementAndGet();
    }

    int getCount() {
        return counter.get();
    }

}
