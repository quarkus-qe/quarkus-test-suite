package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.ping;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.jboss.logging.Logger;

import io.quarkus.oidc.UserInfo;
import io.quarkus.security.identity.SecurityIdentity;

@Path("/userinfo-check")
public class UserinfoResource {

    private static final Logger LOG = Logger.getLogger(UserinfoResource.class);

    @Inject
    UserInfo userInfo;

    @Inject
    SecurityIdentity securityIdentity;

    @GET
    @Path("/me")
    @Produces(MediaType.APPLICATION_JSON)
    public String userInfo() {
        LOG.infof("SecurityIdentity principal: %s", securityIdentity.getPrincipal().getName());
        if (userInfo == null) {
            return "{\"msg\":\"No user info available!\"}";
        }
        LOG.info("JSON RESPONSE IS *** : " + userInfo.getJsonObject().toString());
        return userInfo.getJsonObject().toString();
    }
}
