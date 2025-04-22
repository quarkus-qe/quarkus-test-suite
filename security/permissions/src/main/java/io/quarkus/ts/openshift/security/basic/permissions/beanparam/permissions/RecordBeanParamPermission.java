package io.quarkus.ts.openshift.security.basic.permissions.beanparam.permissions;

import java.security.BasicPermission;
import java.security.Permission;

public class RecordBeanParamPermission extends BasicPermission {
    private final String documentId;
    private final String customAuthorizationHeader;
    private final String accessLevel;
    private final String principalName;

    public RecordBeanParamPermission(String name, String documentId, String customAuthorizationHeader,
            String accessLevel, String principalName) {
        super(name);
        this.documentId = documentId;
        this.customAuthorizationHeader = customAuthorizationHeader;
        this.accessLevel = accessLevel;
        this.principalName = principalName;
    }

    @Override
    public boolean implies(Permission permission) {
        if (principalName != null && principalName.equals("admin")) {
            return true;
        }

        if (permission instanceof AcquiredPermission acquiredPermission) {
            if (!getName().equals(acquiredPermission.getName())) {
                return false;
            }

            return customAuthorizationHeader != null &&
                    customAuthorizationHeader.equals("valid-token") &&
                    documentId != null &&
                    !documentId.isEmpty() &&
                    !"invalid-id".equals(documentId) &&
                    "read".equals(accessLevel);
        }
        return false;
    }

    @Override
    public String getActions() {
        return accessLevel;
    }
}