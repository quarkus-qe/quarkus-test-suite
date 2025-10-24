package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.stepup;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkus.oidc.AuthenticationContext;
import io.quarkus.oidc.AuthorizationCodeFlow;
import io.quarkus.oidc.BearerTokenAuthentication;
import io.quarkus.oidc.Tenant;

@Path("/step-up")
@BearerTokenAuthentication
public class StepUpResource {

    @GET
    @Path("/no-acr")
    @RolesAllowed("user")
    public String noAcrRequired() {
        return "No ACR, but authentication required";
    }

    @GET
    @Path("/no-acr-web-app")
    @AuthorizationCodeFlow
    public String noAcrRequiredWebApp() {
        return "No ACR, but authentication required";
    }

    @GET
    @Path("/single-acr-copper")
    @AuthenticationContext("copper")
    public String singleAcrCopper() {
        return "Single ACR copper validated";
    }

    @GET
    @Path("/single-acr-silver")
    @AuthenticationContext("silver")
    public String singleAcrSilver() {
        return "Single ACR silver validated";
    }

    @AuthorizationCodeFlow
    @GET
    @Path("/single-acr-silver-web-app")
    @AuthenticationContext("silver")
    public String singleAcrSilverWebApp() {
        return "Single ACR silver validated";
    }

    @AuthorizationCodeFlow
    @GET
    @Path("/single-acr-gold-web-app")
    @AuthenticationContext("gold")
    public String singleAcrGoldWebApp() {
        return "Single ACR gold validated";
    }

    @GET
    @Path("/multiple-acr-copper-silver")
    @AuthenticationContext({ "copper", "silver" })
    public String multipleAcrCopperSilver() {
        return "Multiple ACR copper and silver validated";
    }

    @GET
    @Path("/rbac-user-role")
    @AuthenticationContext("silver")
    @RolesAllowed("user")
    public String rbacUserRole() {
        return "ACR and user role validated";
    }

    @GET
    @Path("/max-age-with-acr")
    @AuthenticationContext(value = "silver", maxAge = "PT2M")
    public String maxAgeWithAcr() {
        return "Max age and ACR silver validated";
    }

    @GET
    @Path("/max-age-short")
    @AuthenticationContext(value = "silver", maxAge = "PT1S")
    public String maxAgeShort() {
        return "Max age short with ACR silver validated";
    }

    @GET
    @Path("/max-age-long")
    @AuthenticationContext(value = "silver", maxAge = "PT1H")
    public String maxAgeLong() {
        return "Max age long with ACR silver validated";
    }

    @GET
    @Path("/custom-validator")
    @Tenant("custom-validator")
    public String customValidator() {
        return "Custom validator enforced gold and platinum ACR";
    }

}
