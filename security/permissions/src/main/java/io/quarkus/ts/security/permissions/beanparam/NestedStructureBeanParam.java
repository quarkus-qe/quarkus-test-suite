package io.quarkus.ts.security.permissions.beanparam;

import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;

public class NestedStructureBeanParam {
    @QueryParam("userId")
    private String userId;

    @QueryParam("userName")
    private String userName;

    @QueryParam("resourceId")
    private String resourceId;

    @QueryParam("resourceType")
    private String resourceType;

    private User user;
    private Resource resource;

    @Context
    private SecurityContext securityContext;

    @Context
    public UriInfo uriInfo;

    private User getOrCreateUser() {
        if (user != null) {
            return user;
        }
        user = new User();
        if (userId != null) {
            user.id = userId;
        }
        if (userName != null) {
            user.name = userName;
        }
        return user;
    }

    private Resource getOrCreateResource() {
        if (resource != null) {
            return resource;
        }

        ResourceDetails details = new ResourceDetails(resourceId, resourceType);
        resource = new Resource(details);
        return resource;
    }

    public User getUser() {
        return getOrCreateUser();
    }

    public Resource getResource() {
        return getOrCreateResource();
    }

    public SecurityContext getSecurityContext() {
        return securityContext;
    }

    public String getId() {
        return userId;
    }

    public String getName() {
        return userName;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getPrincipalName() {
        return securityContext != null && securityContext.getUserPrincipal() != null
                ? securityContext.getUserPrincipal().getName()
                : null;
    }

    public static class User {
        public String id;
        public String name;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    public record Resource(ResourceDetails details) {

        public ResourceDetails getDetails() {
            return details;
        }
    }

    public record ResourceDetails(String id, String type) {
        public String getId() {
            return id;
        }

        public String getType() {
            return type;
        }
    }
}