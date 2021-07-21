package io.quarkus.ts.security.vertx.services;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.ts.security.vertx.model.BladeRunner;

@ApplicationScoped
public class BladeRunnerService extends AbstractRedisDao<BladeRunner> {

    private static final String PREFIX = "bladeRunner_";

    public BladeRunnerService() {
        super(PREFIX, BladeRunner.class);
    }
}
