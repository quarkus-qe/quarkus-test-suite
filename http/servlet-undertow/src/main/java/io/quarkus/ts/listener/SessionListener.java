package io.quarkus.ts.listener;

import java.util.LinkedList;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

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
    private LinkedList<String> sessionsBucket = new LinkedList<>();

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
