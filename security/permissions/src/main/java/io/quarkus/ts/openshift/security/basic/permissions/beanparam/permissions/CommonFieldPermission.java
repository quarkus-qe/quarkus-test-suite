package io.quarkus.ts.openshift.security.basic.permissions.beanparam.permissions;

import java.security.BasicPermission;
import java.security.Permission;

public class CommonFieldPermission extends BasicPermission {

    private final String customAuthorizationHeader;
    private final String principalName;

    public CommonFieldPermission(String name, String customAuthorizationHeader, String principalName) {
        super(name);
        this.customAuthorizationHeader = customAuthorizationHeader;
        this.principalName = principalName;
    }

    @Override
    public boolean implies(Permission permission) {
        if (customAuthorizationHeader == null || customAuthorizationHeader.isEmpty()) {
            return false;
        }

        if (principalName != null && principalName.equals("admin")) {
            return customAuthorizationHeader.equals("valid-token");
        }

        if (permission instanceof AcquiredPermission acquiredPermission) {
            if (!getName().equals(acquiredPermission.getName())) {
                return false;
            }

            return customAuthorizationHeader.equals("valid-token");
        }

        return false;
    }
}