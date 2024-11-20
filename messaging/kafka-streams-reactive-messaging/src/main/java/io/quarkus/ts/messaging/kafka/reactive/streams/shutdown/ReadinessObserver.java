package io.quarkus.ts.messaging.kafka.reactive.streams.shutdown;

import jakarta.enterprise.event.Observes;

import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.reactive.messaging.health.HealthReport;
import io.smallrye.reactive.messaging.providers.extension.HealthCenter;

public class ReadinessObserver {

    private static final int SLEEP_PERIOD = 250;

    /**
     * Quarkus starts before channels are ready, but our tests publish messages that need to be delivered.
     * Please find more information in the <a href="https://github.com/quarkusio/quarkus/issues/41441">issue #41441</a>.
     *
     * @param ignored startup event
     * @param healthCenter bean with health reports produced by the Quarkus Messaging
     * @throws InterruptedException if a sleep operation on a local thread fails
     */
    void observe(@Observes StartupEvent ignored, HealthCenter healthCenter) throws InterruptedException {
        Log.info("Delaying application startup until incoming channel 'slow' and outgoing channel 'slow-topic' are ready");
        while (true) {
            final HealthReport healthReport = healthCenter.getReadiness();
            if (areChannelsReady(healthReport)) {
                Log.info("Channels 'slow' and 'slow-topic' are ready, proceeding with application startup");
                return;
            }
            Log.infof("Channel 'slow-topic' or 'slow' is not ready. Going to sleep for %d milliseconds", SLEEP_PERIOD);
            Thread.sleep(SLEEP_PERIOD);
        }
    }

    private static boolean areChannelsReady(HealthReport healthReport) {
        boolean channelSlowReady = false;
        boolean channelSlowTopicReady = false;
        for (HealthReport.ChannelInfo channel : healthReport.getChannels()) {
            String channelName = channel.getChannel();
            if ("slow".equalsIgnoreCase(channelName) && channel.isOk()) {
                channelSlowReady = true;
            } else if ("slow-topic".equalsIgnoreCase(channelName) && channel.isOk()) {
                channelSlowTopicReady = true;
            }
            if (channelSlowReady && channelSlowTopicReady) {
                return true;
            }
        }
        return false;
    }
}
