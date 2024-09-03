package io.quarkus.ts.micrometer.prometheus.kafka;

public final class Channels {

    public static final String CHANNEL_SOURCE_ALERTS = "alerts-source";
    public static final String CHANNEL_TARGET_ALERTS = "alerts-target";
    public static final String ALERTS_STREAM = "alerts-stream";
    public static final String CHANNEL_UNDELIVERABLE_SOURCE = "undeliverable-source";
    public static final String CHANNEL_UNDELIVERABLE_TARGET = "undeliverable-target";

    private Channels() {

    }
}
