package io.quarkus.ts.elasticsearch;

import java.io.IOException;

import org.apache.hc.core5.http.ParseException;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/data-types")
public class DataTypesResource {

    @Inject
    DataTypesService dataTypesService;

    @POST
    public DataTypes indexAndGet(DataTypes dataTypes) throws IOException, ParseException {
        return dataTypesService.indexAndGet(dataTypes);
    }
}
