package io.quarkus.ts.security.permissions.beanparam.permissions;

import java.security.BasicPermission;
import java.security.Permission;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class AcquiredPermission extends BasicPermission {
    public static final String ACTIONS_SEPARATOR = ":";
    private final Set<String> actions;

    public AcquiredPermission(String name, String[] actionsArray) {
        super(name);
        if (actionsArray != null && actionsArray.length != 0) {
            this.actions = processActions(actionsArray);
        } else {
            this.actions = Collections.emptySet();
        }
    }

    private Set<String> processActions(String[] actionsArray) {
        Set<String> actionSet = new HashSet<>(actionsArray.length * 2);

        for (String action : actionsArray) {
            if (action == null || action.trim().isEmpty()) {
                continue;
            }

            actionSet.add(action.trim());
            if (action.contains(ACTIONS_SEPARATOR)) {
                String basePart = action.substring(0, action.indexOf(ACTIONS_SEPARATOR));
                actionSet.add(basePart);
            }
        }
        return Collections.unmodifiableSet(actionSet);
    }

    @Override
    public boolean implies(Permission p) {
        if (p instanceof SimpleBeanParamPermission
                || p instanceof NestedBeanParamPermission
                || p instanceof RecordBeanParamPermission
                || p instanceof CommonFieldPermission) {
            return p.implies(this);
        }
        if (p instanceof AcquiredPermission that) {
            if (!getName().equals(that.getName())) {
                return false;
            }

            if (that.actions.isEmpty()) {
                return true;
            }

            if (this.actions.isEmpty()) {
                return false;
            }

            for (String action : that.actions) {
                if (this.actions.contains(action)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getActions() {
        if (actions.isEmpty()) {
            return "";
        }
        return String.join(",", actions);
    }
}
