package io.quarkus.ts.monitoring.jfr;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.condition.OS;

import io.quarkus.test.bootstrap.Service;

import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;

public class JfrUtils {

    public static final Path RECORDING_PATH = Path.of(System.getProperty("java.io.tmpdir"), "jfrRecording.jfr");

    public static List<RecordedEvent> getRecordedEventsByName(String eventName) throws IOException {
        return RecordingFile.readAllEvents(RECORDING_PATH).stream()
                .filter(e -> e.getEventType().getName().equals(eventName))
                .toList();
    }

    /**
     * This method get the PID and dump the result, before the app is shutdown.
     * It's only on Windows as it's not support `Process#supportsNormalTermination`
     * and thus FW will force the app to terminate by process ID.
     * In most cases the result is same, but when working with JFR the recording needs to be saved.
     * When the app is force to terminate the JFR recording is not saved.
     * There is not much what can be done about it, but this allows the test to at least check
     * if the recorded values are as expected, but not check if Quarkus save them automatically when the app stops.
     */
    public static void dumpJfrRecording(Service service) {
        if (!OS.WINDOWS.isCurrentOs()) {
            return;
        }

        Pattern pattern = Pattern.compile("Use jcmd (\\d+) JFR.dump");
        var pid = service.getLogs().stream()
                .map(pattern::matcher)
                .filter(Matcher::find)
                .map(matcher -> matcher.group(1))
                .findFirst();

        if (pid.isPresent()) {
            try {
                Runtime.getRuntime()
                        .exec(new String[] { "cmd", "/C", "jcmd", pid.get(), "JFR.dump",
                                "filename=" + RECORDING_PATH });
            } catch (Exception e) {
                throw new RuntimeException("Failed to dump the JFR record. The error: " + e);
            }
        }
    }
}
