package io.quarkus.qe.scheduling.spring;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.quarkus.qe.scheduling.spring.services.CounterService;

@Component
public class AnnotationScheduledCounter {

    @Autowired
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