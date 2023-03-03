package io.quarkus.ts.scheduling.quartz.failover;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.Scheduled.ApplicationNotRunning;

@ApplicationScoped
public class AnnotationScheduledJob {

    @ConfigProperty(name = "owner.name")
    String ownerName;

    @Inject
    ExecutionService service;

    @Inject
    InternalApplicationNotRunning appNotRunning;

    @Transactional
    @Scheduled(cron = "0/1 * * * * ?", identity = "my-unique-task")
    void increment() {
        boolean appIsRunning = !appNotRunning.test(null);
        if (appIsRunning) {
            service.addExecution(ownerName);
        }
    }

    // necessary as 'ApplicationNotRunning' is not registered with Quartz extension
    @Singleton
    public static class InternalApplicationNotRunning extends ApplicationNotRunning {

    }

}
