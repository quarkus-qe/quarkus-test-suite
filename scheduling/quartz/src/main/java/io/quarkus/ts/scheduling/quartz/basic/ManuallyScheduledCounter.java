package io.quarkus.ts.scheduling.quartz.basic;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import io.quarkus.runtime.Startup;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkus.ts.scheduling.quartz.basic.services.CounterService;

@Startup
@ApplicationScoped
public class ManuallyScheduledCounter {

    @ConfigProperty(name = "quarkus-qe.enable-manually-scheduled-counter")
    boolean manualSchedulerEnabled;

    @Inject
    Provider<Scheduler> quartz;

    @Inject
    CounterService service;

    public int get() {
        return service.get(caller());
    }

    @PostConstruct
    void init() throws SchedulerException {
        if (manualSchedulerEnabled) {
            JobDetail job = JobBuilder.newJob(CountingJob.class).build();
            Trigger trigger = TriggerBuilder
                    .newTrigger()
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder
                            .simpleSchedule()
                            .withIntervalInSeconds(1)
                            .repeatForever())
                    .build();
            quartz.get().scheduleJob(job, trigger);
        }
    }

    @Singleton
    @RegisterForReflection
    public static class CountingJob implements Job {

        @Inject
        CounterService service;

        @PostConstruct
        void init() {
            service.reset(caller());
        }

        @Override
        public void execute(JobExecutionContext jobExecutionContext) {
            service.invoke(caller());
        }
    }

    private static String caller() {
        return ManuallyScheduledCounter.class.getName();
    }
}
