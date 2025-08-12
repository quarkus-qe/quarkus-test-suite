package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.ping.clients;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.Optional;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;

import io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.model.Score;

import javax.net.ssl.SSLContext;

@RegisterRestClient
@ClientHeaderParam(name = "Authorization", value = "{lookupAuth}")
@Path("/rest-pong")
public interface LookupAuthorizationPongClient {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    String getPong();

    @GET
    @Path("/name/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    String getPongWithPathName(@PathParam("name") String name);

    @POST
    @Path("/withBody")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    String createPongWithBody(Score score);

    @PUT
    @Path("/withBody")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    String updatePongWithBody(Score score);

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    boolean deletePongById(@PathParam("id") String id);

    default String lookupAuth() {
        Config config = ConfigProvider.getConfig();

        String oidcAuthUrl = config.getValue("quarkus.oidc.auth-server-url", String.class);
        String realm = oidcAuthUrl.substring(oidcAuthUrl.lastIndexOf("/") + 1);
        String authUrl = oidcAuthUrl.replace("/realms/" + realm, "");
        String clientId = config.getValue("quarkus.oidc.client-id", String.class);
        String clientSecret = config.getValue("quarkus.oidc.credentials.secret", String.class);

        AuthzClient authzClient = AuthzClient.create(new Configuration(
                authUrl,
                realm,
                clientId,
                Collections.singletonMap("secret", clientSecret),
                prepareHttpClientForAuthzClient(config)));

        return "Bearer " + authzClient.obtainAccessToken("test-user", "test-user").getToken();
    }

    private HttpClient prepareHttpClientForAuthzClient(Config config) {
        Optional<String> truststorePathP12 = config.getOptionalValue("quarkus.tls.keycloak.trust-store.p12.path", String.class);
        Optional<String> truststorePathJks = config.getOptionalValue("quarkus.tls.keycloak.trust-store.jks.path", String.class);
        final SSLConnectionSocketFactory sslConnectionSocketFactory;
        String truststorePassword;
        String trustStorePath;

        if (truststorePathP12.isPresent()) {
            trustStorePath = truststorePathP12.get();
            truststorePassword = config.getValue("quarkus.tls.keycloak.trust-store.p12.password", String.class);
        } else if (truststorePathJks.isPresent()) {
            trustStorePath = truststorePathJks.get();
            truststorePassword = config.getValue("quarkus.tls.keycloak.trust-store.jks.password", String.class);
        } else {
            return HttpClients.createDefault();
        }

        try {
            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(new File(trustStorePath), truststorePassword.toCharArray())
                    .build();
            sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        } catch (CertificateException | IOException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new IllegalStateException("Unable to create SSLConnectionSocketFactory to allow"
                    + " secured connection use self-signed certificates", e);
        }
        return HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build();
    }
}
