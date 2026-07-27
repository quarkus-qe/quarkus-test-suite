package io.quarkus.ts.logging.jboss;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.logmanager.ExtLogRecord;
import org.jboss.logmanager.Level;

import io.quarkus.arc.lookup.LookupIfProperty;
import io.quarkus.logging.json.runtime.JsonFormatter.JsonLogGenerator;
import io.quarkus.logging.json.runtime.JsonProvider;

@ApplicationScoped
@LookupIfProperty(name = "json-provider-test.enabled", stringValue = "true")
public class CdiJsonProvider implements JsonProvider {
    @Override
    public void writeTo(JsonLogGenerator generator, ExtLogRecord record) throws Exception {
        generator.add("customField", "customValue");
        generator.add("loggerNameFromRecord", record.getLoggerName());

        String requestId = record.getMdcCopy().get("requestId");
        if (requestId != null) {
            generator.add("requestIdFromMdc", requestId);
        }

        if (record.getLevel().intValue() >= Level.ERROR.intValue()) {
            generator.add("isErrorRecord", "true");
        }

        generator.startObject("nestedObject")
                .add("nestedField1", "nestedValue1")
                .add("nestedField2", "nestedValue2")
                .endObject();

        generator.add("excludedField", "excludedValue");
    }
}
