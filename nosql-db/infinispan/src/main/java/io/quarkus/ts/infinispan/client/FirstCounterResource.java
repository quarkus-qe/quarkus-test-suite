package io.quarkus.ts.infinispan.client;

import javax.ws.rs.Path;

@Path("/first-counter")
public class FirstCounterResource extends InfinispanCounterResource {
}
