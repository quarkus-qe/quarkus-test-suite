package io.quarkus.ts.http.httpproblem.sources;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.httpproblem.HttpProblem;
import io.quarkiverse.httpproblem.postprocessing.ProblemContext;
import io.quarkiverse.httpproblem.postprocessing.ProblemPostProcessor;

@ApplicationScoped
public class CustomProblemPostProcessor implements ProblemPostProcessor {

    @Override
    public HttpProblem apply(HttpProblem problem, ProblemContext context) {
        return HttpProblem.builder(problem)
                .with("processed", true)
                .build();
    }
}
