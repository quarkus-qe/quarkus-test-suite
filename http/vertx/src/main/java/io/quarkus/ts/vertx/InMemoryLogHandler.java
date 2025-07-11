package io.quarkus.ts.vertx;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.ErrorManager;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import org.jboss.logmanager.ExtHandler;
import org.jboss.logmanager.formatters.PatternFormatter;

public class InMemoryLogHandler extends ExtHandler {
    private static final PatternFormatter FORMATTER = new PatternFormatter(
            "%d{HH:mm:ss} %-5p endpoint_context=%X{endpoint.context} [%c{2.}] (%t) %s%n");

    private static final List<String> recordList = new CopyOnWriteArrayList<>();

    public List<String> logRecords() {
        return Collections.unmodifiableList(recordList);
    }

    public static void reset() {
        recordList.clear();
    }

    @Override
    public void publish(LogRecord record) {
        String loggerName = record.getLoggerName();
        if (loggerName != null && loggerName.equals(ExternalHealthEndpoint.LOGGER_NAME)) {
            final String formatted;
            final Formatter formatter = getFormatter();
            try {
                formatted = formatter.format(record);
            } catch (Exception ex) {
                reportError("Formatting error", ex, ErrorManager.FORMAT_FAILURE);
                return;
            }
            if (!formatted.isEmpty()) {
                recordList.add(formatted);
            }
        }
    }

    @Override
    public Formatter getFormatter() {
        return FORMATTER;
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }

}