package io.quarkus.ts.elasticsearch;

import java.io.IOException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import io.vertx.core.json.JsonObject;

import co.elastic.clients.transport.rest5_client.low_level.Request;
import co.elastic.clients.transport.rest5_client.low_level.Response;
import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;

@ApplicationScoped
public class DataTypesService {
    @Inject
    Rest5Client restClient;

    public DataTypes indexAndGet(DataTypes dataTypes) throws IOException, ParseException {
        Request request = new Request(
                "PUT",
                "/foos/_doc/" + dataTypes.id);
        request.setJsonEntity(JsonObject.mapFrom(dataTypes).toString());
        restClient.performRequest(request);

        request = new Request(
                "GET",
                "/foos/_doc/" + dataTypes.id);
        Response response = restClient.performRequest(request);
        String responseBody = EntityUtils.toString(response.getEntity());

        JsonObject json = new JsonObject(responseBody);
        return json.getJsonObject("_source").mapTo(DataTypes.class);
    }
}
