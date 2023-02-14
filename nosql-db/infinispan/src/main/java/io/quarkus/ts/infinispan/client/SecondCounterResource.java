package io.quarkus.ts.infinispan.client;

import jakarta.ws.rs.Path;

@Path("/second-counter")
public class SecondCounterResource extends InfinispanCounterResource {
}
