package io.quarkus.ts.openshift.security.basic.permissions.beanparam.permissions;

import java.security.BasicPermission;
import java.security.Permission;

public class NestedBeanParamPermission extends BasicPermission {

    private final String id;
    private final String name;
    private final String resourceId;
    private final String resourceType;
    private final String principalName;

    public NestedBeanParamPermission(String permissionName, String id, String name,
            String resourceId, String resourceType, String principalName) {
        super(permissionName);
        this.id = id;
        this.name = name;
        this.resourceId = resourceId;
        this.resourceType = resourceType;
        this.principalName = principalName;
    }

    @Override
    public boolean implies(Permission permission) {
        if ("document".equals(this.resourceType) && (this.name == null || this.name.isEmpty())) {
            return false;
        }

        if ("profile".equals(this.resourceType) && (this.id == null || !this.id.equals(this.resourceId))) {
            return false;
        }

        if (principalName != null && principalName.equals("admin")) {
            return true;
        }

        if (permission instanceof AcquiredPermission acquiredPermission) {
            if (!getName().equals(acquiredPermission.getName())) {
                return false;
            }

            if ("document".equals(this.resourceType)) {
                return isNameValid();
            }

            if ("profile".equals(this.resourceType)) {
                return isIdMatchingResourceId();
            }

            return false;
        }

        return false;
    }

    private boolean isNameValid() {
        return this.name != null && !this.name.isEmpty();
    }

    private boolean isIdMatchingResourceId() {
        return this.id != null && this.id.equals(this.resourceId);
    }
}