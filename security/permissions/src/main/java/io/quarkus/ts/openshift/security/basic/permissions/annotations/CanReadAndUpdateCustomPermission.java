package io.quarkus.ts.openshift.security.basic.permissions.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.quarkus.security.PermissionsAllowed;
import io.quarkus.ts.openshift.security.basic.permissions.resources.helpers.CustomPermission;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@PermissionsAllowed(value = { "read:minimal", "read:all", "update" }, permission = CustomPermission.class)
public @interface CanReadAndUpdateCustomPermission {
}
