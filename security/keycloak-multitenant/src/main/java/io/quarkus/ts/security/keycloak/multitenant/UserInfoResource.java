package io.quarkus.ts.security.keycloak.multitenant;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkus.oidc.OidcTenantConfig;
import io.quarkus.oidc.UserInfo;
import io.quarkus.oidc.runtime.OidcUtils;
import io.quarkus.oidc.runtime.TenantConfigBean;
import io.vertx.ext.web.RoutingContext;

@Path("/user-info")
public class UserInfoResource {

    @Inject
    UserInfo userInfo;

    @Inject
    TenantConfigBean tenantConfigBean;

    @Inject
    RoutingContext routingContext;

    @Path("/default-tenant-random")
    @GET
    public String getDefaultTenantName() {
        if (!tenantConfigBean.getDefaultTenant().oidcConfig().authentication().userInfoRequired().orElse(false)) {
            throw new IllegalStateException("Default tenant user info should be required");
        }

        String tenantId = routingContext.get(OidcUtils.TENANT_ID_ATTRIBUTE);
        if (!OidcUtils.DEFAULT_TENANT_ID.equals(tenantId)) {
            throw new IllegalStateException(
                    "Incorrect tenant resolved based on the path - expected default tenant, got " + tenantId);
        }

        // assert tenant path added in the observer method
        assertTenantPathsContain("/extra-default-tenant-path");
        // assert tenant path added in the application.properties
        assertTenantPathsContain("/user-info/default-tenant-random");
        return userInfo.getPreferredUserName();
    }

    @Path("/named-tenant-random")
    @GET
    public String getNamedTenantName() {
        OidcTenantConfig namedTenantConfig = tenantConfigBean.getStaticTenant("named").oidcConfig();

        if (!namedTenantConfig.authentication().userInfoRequired().orElse(false)) {
            throw new IllegalStateException("Named tenant user info should be required");
        }
        String tenantId = routingContext.get(OidcUtils.TENANT_ID_ATTRIBUTE);
        if (!"named".equals(tenantId)) {
            throw new IllegalStateException(
                    "Incorrect tenant resolved based on the path - expected 'named', got " + tenantId);
        }
        assertTenantPathsContain("/user-info/named-tenant-random");
        return userInfo.getPreferredUserName();
    }

    private void assertTenantPathsContain(String tenantPath) {
        OidcTenantConfig tenantConfig = routingContext.get(OidcTenantConfig.class.getName());
        if (!tenantConfig.tenantPaths().get().contains(tenantPath)) {
            throw new IllegalStateException("Tenant config does not contain the tenant path " + tenantPath);
        }
    }
}
