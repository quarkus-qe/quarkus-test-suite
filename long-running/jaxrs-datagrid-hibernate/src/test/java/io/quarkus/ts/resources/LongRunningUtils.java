package io.quarkus.ts.resources;

import static java.lang.System.getProperty;
import static java.time.Duration.ofMillis;
import static java.time.Duration.ofMinutes;
import static java.time.temporal.ChronoUnit.SECONDS;

import java.time.Duration;
import java.time.Instant;

public class LongRunningUtils {

    private static final String LONG_RUNNING_NAME = "ts.long-running.duration.min";
    private static final String LONG_RUNNING_POLL_INTERVAL_NAME = "ts.long-running.poll.ms";
    private static final String LONG_RUNNING_COOL_DOWN_NAME = "ts.long-running.cool-down.duration.min";
    private static final String LONG_RUNNING_COOL_DOWN_DEFAULT_VALUE = "1";
    private static final String DEFAULT_TEST_DURATION_MIN = "1";
    private static final String DEFAULT_POLL_INTERVAL_MS = "100";

    private final Duration timeout;
    private final Duration pollInterval;
    private Instant targetTime;
    private Instant coolDownTargetTime;
    private final Duration coolDown;

    public LongRunningUtils() {
        // Retrieve test duration
        int testDurationMin = Integer.parseInt(getProperty(LONG_RUNNING_NAME, DEFAULT_TEST_DURATION_MIN));
        timeout = ofMinutes(testDurationMin);

        // Retrieve poll interval
        pollInterval = ofMillis(Integer.parseInt(getProperty(LONG_RUNNING_POLL_INTERVAL_NAME, DEFAULT_POLL_INTERVAL_MS)));

        // Retrieve cool down duration
        int coolDownMin = Integer.parseInt(getProperty(LONG_RUNNING_COOL_DOWN_NAME, LONG_RUNNING_COOL_DOWN_DEFAULT_VALUE));
        coolDown = ofMinutes(coolDownMin);
    }

    public long ttl(Instant target, Duration timeout) {
        return SECONDS.between(Instant.now(), target.plusSeconds(timeout.toSeconds()));
    }

    public boolean isCompleted() {
        if (targetTime == null) {
            targetTime = Instant.now();
        }

        return ttl(targetTime, timeout) < 10;
    }

    public boolean isCoolDownCompleted() {
        if (coolDownTargetTime == null) {
            coolDownTargetTime = Instant.now();
        }

        return ttl(coolDownTargetTime, coolDown) < 10;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public Duration getPollInterval() {
        return pollInterval;
    }

    public Duration getCoolDown() {
        return coolDown;
    }
}
