package io.quarkus.ts.infinispan.client;

import javax.ws.rs.Path;

@Path("/second-counter")
public class SecondCounterResource extends InfinispanCounterResource {
}
