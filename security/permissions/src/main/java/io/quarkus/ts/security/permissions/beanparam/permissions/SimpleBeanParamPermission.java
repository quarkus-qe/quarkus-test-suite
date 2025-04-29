package io.quarkus.ts.security.permissions.beanparam.permissions;

import java.security.BasicPermission;
import java.security.Permission;

public class SimpleBeanParamPermission extends BasicPermission {

    private final String resourceId;
    private final String customAuthorizationHeader;
    private final String action;
    private final String principalName;

    public SimpleBeanParamPermission(String name, String resourceId, String customAuthorizationHeader,
            String action, String principalName) {
        super(name);
        this.resourceId = resourceId;
        this.customAuthorizationHeader = customAuthorizationHeader;
        this.action = action;
        this.principalName = principalName;
    }

    @Override
    public boolean implies(Permission permission) {
        if ("invalid-action".equals(action)) {
            return false;
        }

        if (principalName != null && principalName.equals("admin")) {
            return true;
        }

        if (permission instanceof AcquiredPermission acquiredPermission) {
            if (!getName().equals(acquiredPermission.getName())) {
                return false;
            }

            boolean hasValidToken = customAuthorizationHeader != null &&
                    customAuthorizationHeader.equals("valid-token");
            boolean hasValidAction = "basic".equals(action) ||
                    "read".equals(action) ||
                    "write".equals(action);

            return hasValidToken && hasValidAction;
        }
        return false;
    }

    @Override
    public String getActions() {
        return action;
    }
}
