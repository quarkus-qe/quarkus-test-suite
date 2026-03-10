package io.quarkus.qe.hibernate.spatial.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import io.quarkus.qe.hibernate.spatial.service.GeolatteSpatialDatasetService;
import io.quarkus.qe.hibernate.spatial.service.JtsSpatialDatasetService;

@Path("/spatial/init")
public class SpatialInitResource {

    @Inject
    JtsSpatialDatasetService jtsDataset;

    @Inject
    GeolatteSpatialDatasetService geolatteDataset;

    @POST
    public void init() {
        jtsDataset.init();
        geolatteDataset.init();
    }
}
