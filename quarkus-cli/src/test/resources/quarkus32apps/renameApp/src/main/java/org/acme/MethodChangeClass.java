package org.acme;

import io.smallrye.mutiny.groups.UniAndGroup4;
import io.smallrye.mutiny.groups.UniMemoize;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.logging.LogRecord;

/**
 * This class does not work.
 * It is used just for "quarkus update" command, to check if it renames methods correctly
 */
public class MethodChangeClass {
    UniMemoize uniMemoize;

    UniAndGroup4 uniAndGroup;

    LogRecord logRecord;

    public void foo(){
        uniMemoize.atLeast(Duration.of(5, ChronoUnit.SECONDS));
        uniAndGroup.combinedWith(o -> null);

        logRecord.getThreadID();
        logRecord.setThreadID(0);
    }
}
