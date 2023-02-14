package io.quarkus.ts.messaging.qpid;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.Session;

/**
 * https://quarkus.io/guides/jms#qpid-jms-amqp.
 */
@ApplicationScoped
public class PriceProducer implements Runnable {

    public static final int PRICES_MAX = 100;

    private final Random random = new Random();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private ConnectionFactory connectionFactory;

    void onStart(@Observes StartupEvent ev, ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        this.scheduler.scheduleWithFixedDelay(this, 0L, 1L, TimeUnit.SECONDS);
    }

    void onStop(@Observes ShutdownEvent ev) {
        scheduler.shutdown();
    }

    @Override
    public void run() {
        try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {
            context.createProducer().send(context.createTopic("prices"), Integer.toString(random.nextInt(PRICES_MAX)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
