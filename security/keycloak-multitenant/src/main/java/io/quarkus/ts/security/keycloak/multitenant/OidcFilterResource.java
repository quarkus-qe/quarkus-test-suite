package io.quarkus.ts.security.keycloak.multitenant;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.quarkus.oidc.TenantFeature;
import io.quarkus.ts.security.keycloak.multitenant.filters.AuthorizationCodeFlowRequestFilter;
import io.quarkus.ts.security.keycloak.multitenant.filters.AuthorizationCodeFlowResponseFilter;
import io.quarkus.ts.security.keycloak.multitenant.filters.BearerTokenRequestFilter;
import io.quarkus.ts.security.keycloak.multitenant.filters.BearerTokenResponseFilter;
import io.quarkus.ts.security.keycloak.multitenant.filters.CombinedBearerServiceFilter;
import io.quarkus.ts.security.keycloak.multitenant.filters.GlobalCompatibilityFilter;
import io.quarkus.ts.security.keycloak.multitenant.filters.MultiTenantFeatureFilter;

@Path("/oidc-filter-state")
@Produces(MediaType.APPLICATION_JSON)
public class OidcFilterResource {

    @Inject
    BearerTokenRequestFilter bearerTokenRequestFilter;

    @Inject
    BearerTokenResponseFilter bearerTokenResponseFilter;

    @Inject
    AuthorizationCodeFlowRequestFilter authorizationCodeFlowRequestFilter;

    @Inject
    AuthorizationCodeFlowResponseFilter authorizationCodeFlowResponseFilter;

    @Inject
    @TenantFeature({ "service-tenant", "jwt-tenant" })
    MultiTenantFeatureFilter multiTenantFeatureFilter;

    @Inject
    @TenantFeature("service-tenant")
    CombinedBearerServiceFilter combinedBearerServiceFilter;;

    @Inject
    GlobalCompatibilityFilter globalCompatibilityFilter;

    @GET
    public FilterState getState() {
        return new FilterState(
                bearerTokenRequestFilter.isCalled(),
                bearerTokenResponseFilter.isCalled(),
                authorizationCodeFlowRequestFilter.isCalled(),
                authorizationCodeFlowResponseFilter.isCalled(),
                combinedBearerServiceFilter.isCalled(),
                globalCompatibilityFilter.isCalled());
    }

    @POST
    @Path("/reset")
    public Response reset() {
        bearerTokenRequestFilter.reset();
        bearerTokenResponseFilter.reset();
        globalCompatibilityFilter.reset();
        authorizationCodeFlowRequestFilter.reset();
        authorizationCodeFlowResponseFilter.reset();
        combinedBearerServiceFilter.reset();
        return Response.noContent().build();
    }

    public record FilterState(
            boolean bearerRequest,
            boolean bearerResponse,
            boolean authorizationCodeFlowRequest,
            boolean authorizationCodeFlowResponse,
            boolean combinedFilterCalled,
            boolean globalFilterCalled

    ) {
    }

    @GET
    @Path("/global")
    public GlobalCompatibilityFilterState getGlobalState() {
        return new GlobalCompatibilityFilterState(
                globalCompatibilityFilter.getInvocationCount(),
                globalCompatibilityFilter.getLastTenantId());
    }

    @GET
    @Path("/combined")
    public CombinedFilterState getCombinedState() {
        return new CombinedFilterState(
                combinedBearerServiceFilter.isCalled(),
                combinedBearerServiceFilter.getCapturedTenantId());
    }

    @GET
    @Path("/multi-tenant")
    public boolean getMultiTenantState() {
        return multiTenantFeatureFilter.isCalled();
    }

    public record GlobalCompatibilityFilterState(int invocationCount, String lastTenantId) {
    }

    public record CombinedFilterState(boolean called, String tenantId) {
    }

}
