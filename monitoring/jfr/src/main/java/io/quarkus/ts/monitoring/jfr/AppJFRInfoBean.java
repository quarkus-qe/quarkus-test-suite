package io.quarkus.ts.monitoring.jfr;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.runtime.Startup;

import jdk.jfr.FlightRecorder;

// TODO: Remove this when https://github.com/quarkusio/quarkus/issues/50392 / https://issues.redhat.com/browse/QUARKUS-6687
// This ensure that event are stored at start even when the full chunk is not rotated.
@ApplicationScoped
public class AppJFRInfoBean {

    @Startup
    void startupTask() {
        FlightRecorder.getFlightRecorder().takeSnapshot();
    }
}
