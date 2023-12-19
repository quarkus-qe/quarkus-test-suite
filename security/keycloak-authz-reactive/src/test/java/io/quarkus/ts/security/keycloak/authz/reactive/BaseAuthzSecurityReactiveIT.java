package io.quarkus.ts.security.keycloak.authz.reactive;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.keycloak.authorization.client.AuthzClient;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.vertx.core.http.HttpMethod;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpRequest;
import io.vertx.mutiny.ext.web.client.HttpResponse;

public abstract class BaseAuthzSecurityReactiveIT {

    static final String NORMAL_USER = "test-normal-user";
    static final String ADMIN_USER = "test-admin-user";
    static final String CLIENT_ID_DEFAULT = "test-application-client";
    static final String CLIENT_SECRET_DEFAULT = "test-application-client-secret";

    private AuthzClient authzClient;
    private UniAssertSubscriber<HttpResponse<Buffer>> response;

    @BeforeEach
    public void setup() {
        authzClient = getKeycloak().createAuthzClient(CLIENT_ID_DEFAULT, CLIENT_SECRET_DEFAULT);
    }

    @Tag("QUARKUS-1257")
    @Test
    public void genericAndExtendedSecuredEndpointShouldResponseOk() {
        String bearerToken = getToken(NORMAL_USER, NORMAL_USER);

        whenMakeRequestTo(HttpMethod.GET, "/user-details", bearerToken);
        thenStatusCodeIs(HttpStatus.SC_OK);
        thenBodyIs(NORMAL_USER);

        whenMakeRequestTo(HttpMethod.POST, "/user-details/advanced-specific", bearerToken);
        thenStatusCodeIs(HttpStatus.SC_OK);
        thenBodyIs(NORMAL_USER);

        whenMakeRequestTo(HttpMethod.POST, "/user-details/advanced", bearerToken);
        thenStatusCodeIs(HttpStatus.SC_OK);
        thenBodyIs(NORMAL_USER);
    }

    @Test
    public void normalUserUserResource() {
        whenMakeRequestTo(HttpMethod.GET, "/user", getToken(NORMAL_USER, NORMAL_USER));
        thenStatusCodeIs(HttpStatus.SC_OK);
        thenBodyIs("Hello, user " + NORMAL_USER);
    }

    @Test
    public void normalUserUserResourceIssuer() {
        whenMakeRequestTo(HttpMethod.GET, "/user/issuer", getToken(NORMAL_USER, NORMAL_USER));
        thenStatusCodeIs(HttpStatus.SC_OK);
        thenBodyStartWith("user token issued by " + getKeycloak().getURI(Protocol.HTTP).getRestAssuredStyleUri());
    }

    @Test
    public void normalUserAdminResource() {
        whenMakeRequestTo(HttpMethod.GET, "/admin", getToken(NORMAL_USER, NORMAL_USER));
        thenStatusCodeIs(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void adminUserUserResource() {
        whenMakeRequestTo(HttpMethod.GET, "/user", getToken(ADMIN_USER, ADMIN_USER));
        thenStatusCodeIs(HttpStatus.SC_OK);
        thenBodyIs("Hello, user " + ADMIN_USER);
    }

    @Test
    public void adminUserAdminResource() {
        whenMakeRequestTo(HttpMethod.GET, "/admin", getToken(ADMIN_USER, ADMIN_USER));
        thenStatusCodeIs(HttpStatus.SC_OK);
        thenBodyIs("Hello, admin " + ADMIN_USER);
    }

    @Test
    public void adminUserAdminResourceIssuer() {
        whenMakeRequestTo(HttpMethod.GET, "/admin/issuer", getToken(ADMIN_USER, ADMIN_USER));
        thenStatusCodeIs(HttpStatus.SC_OK);
        thenBodyStartWith("admin token issued by " + getKeycloak().getURI(Protocol.HTTP).getRestAssuredStyleUri());
    }

    @Test
    public void noUserUserResource() {
        whenMakeRequestTo(HttpMethod.GET, "/user", "");
        thenStatusCodeIs(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void noUserAdminResource() {
        whenMakeRequestTo(HttpMethod.GET, "/admin", "");
        thenStatusCodeIs(HttpStatus.SC_UNAUTHORIZED);
    }

    private void thenBodyIs(String expectedBody) {
        String bodyAsString = response.awaitItem().assertCompleted().getItem().bodyAsString();
        assertThat(expectedBody, equalTo(bodyAsString));
    }

    private void thenBodyStartWith(String expectedBody) {
        String bodyAsString = response.awaitItem().assertCompleted().getItem().bodyAsString();
        assertThat(bodyAsString, startsWith(expectedBody));
    }

    private void thenStatusCodeIs(int expectedCode) {
        int statusCode = response.awaitItem().assertCompleted().getItem().statusCode();
        assertThat(expectedCode, equalTo(statusCode));
    }

    private void whenMakeRequestTo(HttpMethod method, String path, String bearerToken) {
        HttpRequest<Buffer> req = createHttpRequest(method, path);
        if (StringUtils.isNotBlank(bearerToken)) {
            addHttpRequestBearerToken(req, bearerToken);
        }

        response = req.send().subscribe().withSubscriber(UniAssertSubscriber.create());
    }

    private HttpRequest<Buffer> createHttpRequest(HttpMethod method, String path) {
        return getApp().mutiny().request(method, path);
    }

    private HttpRequest<Buffer> addHttpRequestBearerToken(HttpRequest<Buffer> req, String token) {
        return req.bearerTokenAuthentication(token);
    }

    protected abstract KeycloakService getKeycloak();

    protected abstract RestService getApp();

    private String getToken(String userName, String password) {
        return authzClient.obtainAccessToken(userName, password).getToken();
    }
}
