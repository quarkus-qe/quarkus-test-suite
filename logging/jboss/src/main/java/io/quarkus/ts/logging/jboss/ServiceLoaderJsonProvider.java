package io.quarkus.ts.logging.jboss;

import org.jboss.logmanager.ExtLogRecord;

import io.quarkus.logging.json.runtime.JsonFormatter.JsonLogGenerator;
import io.quarkus.logging.json.runtime.JsonProvider;

public class ServiceLoaderJsonProvider implements JsonProvider {
    @Override
    public void writeTo(JsonLogGenerator generator, ExtLogRecord record) throws Exception {
        generator.add("serviceLoaderField", "serviceLoaderValue");
    }
}
