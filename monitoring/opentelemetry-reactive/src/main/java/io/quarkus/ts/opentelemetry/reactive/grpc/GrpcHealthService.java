package io.quarkus.ts.opentelemetry.reactive.grpc;

import io.quarkus.example.HealthCheckRequest;
import io.quarkus.example.HealthCheckResponse;
import io.quarkus.example.HealthService;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

@GrpcService
// todo this should return statuses, but it isn't failing the deployment
public class GrpcHealthService implements HealthService {

    @Override
    public Uni<HealthCheckResponse> check(HealthCheckRequest request) {
        return Uni.createFrom().failure(new RuntimeException("Error!"));
    }

    @Override
    public Multi<HealthCheckResponse> watch(HealthCheckRequest request) {
        return Multi.createFrom().failure(new RuntimeException("Error!"));
    }
}
