package io.quarkus.ts.infinispan.client;

import jakarta.ws.rs.Path;

@Path("/first-counter")
public class FirstCounterResource extends InfinispanCounterResource {
}
