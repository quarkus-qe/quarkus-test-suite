package io.quarkus.ts.elasticsearch;

import java.io.IOException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import io.vertx.core.json.JsonObject;

@ApplicationScoped
public class DataTypesService {
    @Inject
    RestClient restClient;

    public DataTypes indexAndGet(DataTypes dataTypes) throws IOException {
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
