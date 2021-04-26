package io.quarkus.ts.openshift.microprofile;

import io.opentracing.Tracer;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.context.ManagedExecutor;

@ApplicationScoped
public class HelloService {
    @Inject
    ManagedExecutor executor;

    @Inject
    Tracer tracer;

    public CompletionStage<String> get(String name) {
        tracer.activeSpan().log("HelloService called");
        return executor.supplyAsync(() -> {
            tracer.activeSpan().log("HelloService async processing");
            return "Hello, " + name + "!";
        });
    }
}
