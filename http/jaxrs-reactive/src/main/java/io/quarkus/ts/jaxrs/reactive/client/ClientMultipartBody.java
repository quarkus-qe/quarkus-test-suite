package io.quarkus.ts.jaxrs.reactive.client;

import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;

public class ClientMultipartBody {

    @RestForm("pojoData")
    @PartType(MediaType.APPLICATION_JSON)
    public PojoData pojoData;
}
