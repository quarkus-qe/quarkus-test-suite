package io.quarkus.ts.security.vertx.services;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.ts.security.vertx.model.Replicant;

@ApplicationScoped
public class ReplicantService extends AbstractRedisDao<Replicant> {

    private static final String PREFIX = "replicant_";

    public ReplicantService() {
        super(PREFIX, Replicant.class);
    }
}
