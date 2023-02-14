package io.quarkus.ts.scheduling.quartz.basic;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkus.scheduler.Scheduled;
import io.quarkus.ts.scheduling.quartz.basic.services.CounterService;

@ApplicationScoped
public class AnnotationScheduledCounter {

    @Inject
    CounterService service;

    @PostConstruct
    void init() {
        service.reset(caller());
    }

    public int get() {
        return service.get(caller());
    }

    @Scheduled(cron = "0/1 * * * * ?")
    void increment() {
        service.invoke(caller());
    }

    private static final String caller() {
        return AnnotationScheduledCounter.class.getName();
    }

}