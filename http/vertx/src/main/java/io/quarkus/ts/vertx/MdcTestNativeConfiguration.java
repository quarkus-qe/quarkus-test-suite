package io.quarkus.ts.vertx;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(targets = {
        ExternalHealthEndpoint.class,
}, classNames = {
        "org.eclipse.parsson.JsonStringImpl",
})
public class MdcTestNativeConfiguration {
}
