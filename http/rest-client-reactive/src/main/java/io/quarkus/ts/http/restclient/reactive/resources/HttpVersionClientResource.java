package io.quarkus.ts.http.restclient.reactive.resources;

import java.net.MalformedURLException;
import java.net.URI;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.client.impl.ClientResponseImpl;

import io.quarkus.ts.http.restclient.reactive.HttpVersionClient;
import io.quarkus.ts.http.restclient.reactive.HttpVersionClientWithConfigKey;
import io.quarkus.ts.http.restclient.reactive.HttpsVersionClientWithConfigKey;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpVersion;

@Path("/http2")
public class HttpVersionClientResource {

    public static final String WRONG_HTTP_VERSION = "The HTTP version should be HTTP_2 but is ";
    public static final String NAME = "Rest Client";
    private static final int TIMEOUT_SECONDS = 10;
    private final URI baseUri;
    private final URI securedBaseUri;

    @Inject
    @RestClient
    HttpVersionClientWithConfigKey httpVersionClientWithConfigKey;

    @Inject
    @RestClient
    HttpsVersionClientWithConfigKey httpsVersionClientWithConfigKey;

    public HttpVersionClientResource(@ConfigProperty(name = "quarkus.http.port") int httpPort,
            @ConfigProperty(name = "quarkus.http.ssl-port") int sslHttpPort,
            @ConfigProperty(name = "quarkus.http.host") String baseHost) {
        this.baseUri = URI.create("http://" + baseHost + ":" + httpPort);
        this.securedBaseUri = URI.create("https://" + baseHost + ":" + sslHttpPort);
    }

    @GET
    @Path("https-synchronous")
    public Response httpsSynchronous() throws MalformedURLException {
        return createSynchronousResponseForClient(securedBaseUri);
    }

    @GET
    @Path("http-synchronous")
    public Response httpSynchronous() throws MalformedURLException {
        return createSynchronousResponseForClient(baseUri);
    }

    @GET
    @Path("https-asynchronous")
    public Uni<Response> httpsAsynchronous() throws MalformedURLException {
        return createAsynchronousResponseForClient(securedBaseUri);
    }

    @GET
    @Path("http-asynchronous")
    public Uni<Response> httpAsynchronous() throws MalformedURLException {
        return createAsynchronousResponseForClient(baseUri);
    }

    @GET
    @Path("https-synchronous-for-client-with-key")
    public Response httpsSynchronousForClientWithKey() {
        Response response = httpsVersionClientWithConfigKey.getClientHttpVersion(NAME);
        return checkHttpVersion(response);
    }

    @GET
    @Path("http-synchronous-for-client-with-key")
    public Response httpSynchronousForClientWithKey() {
        Response response = httpVersionClientWithConfigKey.getClientHttpVersion(NAME);
        return checkHttpVersion(response);
    }

    @GET
    @Path("https-asynchronous-for-client-with-key")
    public Uni<Response> httpsAsynchronousForClientWithKey() {
        Uni<Response> response = httpsVersionClientWithConfigKey.getClientHttpVersionAsync(NAME);

        return checkHttpVersionAsync(response);
    }

    @GET
    @Path("http-asynchronous-for-client-with-key")
    public Uni<Response> httpAsynchronousForClientWithKey() {
        Uni<Response> response = httpVersionClientWithConfigKey.getClientHttpVersionAsync(NAME);

        return checkHttpVersionAsync(response);
    }

    /**
     * Create HttpVersionClient and make REST client synchronous request to `httpVersion/synchronous endpoint`
     * Response from the client is checked if HTTP/2 was used
     *
     * @param uri base url for client to use
     * @return Response contain original response from endpoint or error message containing actual http version used
     */
    private Response createSynchronousResponseForClient(URI uri) throws MalformedURLException {
        HttpVersionClient httpVersionClient = RestClientBuilder.newBuilder()
                .baseUrl(uri.toURL())
                .build(HttpVersionClient.class);

        Response response = httpVersionClient.getClientHttpVersion(NAME);
        return checkHttpVersion(response);
    }

    /**
     * Create HttpVersionClient and make REST client asynchronous request to `httpVersion/asynchronous endpoint`
     * Response from the client is checked if HTTP/2 was used
     *
     * @param uri base url for client to use
     * @return Response contain original response from endpoint or error message containing actual http version used
     */
    private Uni<Response> createAsynchronousResponseForClient(URI uri) throws MalformedURLException {
        HttpVersionClient httpVersionClient = RestClientBuilder.newBuilder()
                .baseUrl(uri.toURL())
                .build(HttpVersionClient.class);

        Uni<Response> response = httpVersionClient.getClientHttpVersionAsync(NAME);
        return checkHttpVersionAsync(response);
    }

    /**
     * Check response from the REST client if HTTP/2 is used, and it's body contain HTTP/2 version string
     *
     * @param response response of REST client
     * @return Response contains original response from endpoint or error message containing actual http version used
     */
    private Response checkHttpVersion(Response response) {
        String httpVersion = ((ClientResponseImpl) response).getHttpVersion();
        response.bufferEntity();
        String responseText = response.readEntity(String.class);
        if (httpVersion.equals(HttpVersion.HTTP_2.name()) && responseText.contains(HttpVersion.HTTP_2.name())) {
            return response;
        }

        return Response.ok(WRONG_HTTP_VERSION + httpVersion).build();
    }

    /**
     * Check response from the REST client if HTTP/2 is used, and it's body contain HTTP/2 version string
     *
     * @param response uni response of REST client
     * @return Uni<Response> contains original response from endpoint or error message containing actual http version used
     */
    private Uni<Response> checkHttpVersionAsync(Uni<Response> response) {
        return response.onItem().transformToUni(r -> {
            String httpVersion = ((ClientResponseImpl) r).getHttpVersion();
            String responseText = r.readEntity(String.class);
            if (httpVersion.equals(HttpVersion.HTTP_2.name()) && responseText.contains(HttpVersion.HTTP_2.name())) {
                return response;
            }
            return Uni.createFrom().item(Response.ok(WRONG_HTTP_VERSION + httpVersion).build());
        });
    }
}
