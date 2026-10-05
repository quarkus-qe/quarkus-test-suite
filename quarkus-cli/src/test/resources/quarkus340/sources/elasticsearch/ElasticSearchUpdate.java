package org.acme;

import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

public class ElasticSearchUpdate {
    private RestClient client;
    private RestClientBuilder builder;

    public void setClient(RestClient client) {
        this.client = client;
    }

    public Response search() throws Exception {
        Request request = new Request("GET", "/index/_search");
        return client.performRequest(request);
    }

    public class MyConfigurator implements RestClientBuilder.HttpClientConfigCallback {
        @Override
        public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
            return null;
        }
    }
}
