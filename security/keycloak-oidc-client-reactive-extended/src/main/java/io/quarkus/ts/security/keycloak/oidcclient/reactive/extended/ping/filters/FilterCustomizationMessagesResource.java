package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.ping.filters;

import java.util.List;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/filter-customization-messages")
public class FilterCustomizationMessagesResource {

    @Path("/request")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getRequestFilterMessages() {

        return CustomTokenRequestBodyFilter.interceptedMessageLogs;
    }

    @Path("/response")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getResponseFilterMessages() {
        return CustomTokenResponseBodyFilter.interceptedMessageLogs;
    }

    @Path("/chained-request")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getChainedRequestFilterMessages() {
        return ChainedParameterRequestFilter.interceptedMessageLogs;
    }

    @Path("/keycloak-userinfo")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getKeycloakUserInfoFilterMessages() {
        return KeycloakUserInfoFilter.interceptedMessageLogs;
    }

    @Path("/client-registration-request")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getClientRegistrationRequestMessages() {
        return ClientRegistrationRequestFilter.interceptedMessageLogs;
    }

    @Path("/client-registration-response")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getClientRegistrationResponseMessages() {
        return ClientRegistrationResponseFilter.interceptedMessageLogs;
    }

    @Path("/clear")
    @DELETE
    public void clearLogs() {
        CustomTokenRequestBodyFilter.interceptedMessageLogs.clear();
        CustomTokenResponseBodyFilter.interceptedMessageLogs.clear();
        ChainedParameterRequestFilter.interceptedMessageLogs.clear();
        ClientRegistrationRequestFilter.interceptedMessageLogs.clear();
        ClientRegistrationResponseFilter.interceptedMessageLogs.clear();
    }

}
