package io.quarkus.ts.http.undertow.listener;

import java.util.concurrent.ConcurrentLinkedDeque;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;

@ApplicationScoped
public final class SessionListener implements HttpSessionListener {

    public static final String GAUGE_ACTIVE_SESSION = "io_quarkus_ts_active_sessions_amount";
    /*
     * Needs to use thread-safe collection.
     * When multiple sessions gets created or destroyed at once, it might create inconsistencies otherwise.
     */
    private final ConcurrentLinkedDeque<String> sessionsBucket = new ConcurrentLinkedDeque<>();

    SessionListener(MeterRegistry registry) {
        registry.gaugeCollectionSize(GAUGE_ACTIVE_SESSION, Tags.empty(), sessionsBucket);
        new JvmMemoryMetrics().bindTo(registry);
        new JvmGcMetrics().bindTo(registry);
        new ProcessorMetrics().bindTo(registry);
        new JvmThreadMetrics().bindTo(registry);
        new ClassLoaderMetrics().bindTo(registry);
    }

    public void sessionCreated(HttpSessionEvent event) {
        HttpSession session = event.getSession();
        sessionsBucket.add(session.getId());
    }

    public void sessionDestroyed(HttpSessionEvent event) {
        sessionsBucket.remove(event.getSession().getId());
    }
}
