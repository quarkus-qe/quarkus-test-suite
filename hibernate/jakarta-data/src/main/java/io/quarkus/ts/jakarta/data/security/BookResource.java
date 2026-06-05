package io.quarkus.ts.jakarta.data.security;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import io.quarkus.ts.jakarta.data.db.MyBook;

@Path("/book")
public class BookResource {
    @Inject
    BookRepository repository;

    @Inject
    BookMethodSecuredRepository methodSecurityRepository;

    @Inject
    BookTypeRoleRepository typeRoleRepository;

    @Inject
    BookTypeAuthenticatedRepository typeAuthenticatedRepository;

    @Inject
    BookTypeDenyAllRepository typeDenyAllRepository;

    @Inject
    BookTypePermissionRepository typePermissionRepository;

    @Inject
    BookInheritedTypeSecuredRepository inheritedTypeSecurityRepository;

    @Inject
    BookInheritedMethodSecuredRepository inheritedMethodSecurityRepository;

    @Inject
    BookPrecedenceRepository precedenceRepository;

    @POST
    @Path("/add")
    @Transactional
    public void add(MyBook book) {
        repository.insert(book);
    }

    @GET
    @Path("{title}")
    public MyBook findByTitle(@PathParam("title") String title) {
        return repository.findByTitle(title);
    }

    @POST
    @Path("/add/role")
    @Transactional
    public void addWithWriterRole(MyBook book) {
        methodSecurityRepository.insertWithWriterRole(book);
    }

    @POST
    @Path("/add/permission")
    @Transactional
    public void addWithPermission(MyBook book) {
        methodSecurityRepository.insertWithPermission(book);
    }

    @PUT
    @Path("/{title}/update/writer")
    @Transactional
    public MyBook updateWithWriterRole(@PathParam("title") String title, MyBook updatedBook) {
        MyBook book = repository.findByTitle(title);
        book.title = updatedBook.title;
        methodSecurityRepository.updateWithWriterRole(book);
        return book;
    }

    @DELETE
    @Path("/{title}/delete/permission")
    @Transactional
    public void deleteWithPermission(@PathParam("title") String title) {
        MyBook book = repository.findByTitle(title);
        methodSecurityRepository.deleteWithPermission(book);
    }

    @GET
    @Path("/{title}/method-authenticated")
    public MyBook findWithMethodLevelAuthenticated(@PathParam("title") String title) {
        return methodSecurityRepository.findWithAuthenticated(title);
    }

    @GET
    @Path("/{title}/method-denyAll")
    public MyBook findWithMethodLevelDenyAll(@PathParam("title") String title) {
        return methodSecurityRepository.findWithDenyAll(title);
    }

    @GET
    @Path("/{title}/method-permission")
    public MyBook findWithMethodLevelPermission(@PathParam("title") String title) {
        return methodSecurityRepository.findWithPermission(title);
    }

    @GET
    @Path("/{title}/method-multiple-permissions")
    public MyBook findWithMethodLevelMultiplePermissions(@PathParam("title") String title) {
        return methodSecurityRepository.findWithMultiplePermissions(title);
    }

    @GET
    @Path("/{title}/type-role")
    public MyBook findWithTypeLevelRole(@PathParam("title") String title) {
        return typeRoleRepository.findWithTypeLevelRole(title);
    }

    @GET
    @Path("/{title}/type-authenticated")
    public MyBook findWithTypeLevelAuthenticated(@PathParam("title") String title) {
        return typeAuthenticatedRepository.findWithTypeLevelAuthenticated(title);
    }

    @GET
    @Path("/{title}/type-denyAll")
    public MyBook findWithTypeLevelDenyAll(@PathParam("title") String title) {
        return typeDenyAllRepository.findWithTypeLevelDenyAll(title);
    }

    @GET
    @Path("/{title}/type-permission")
    public MyBook findWithTypeLevelPermission(@PathParam("title") String title) {
        return typePermissionRepository.findWithTypeLevelPermission(title);
    }

    @GET
    @Path("/{title}/inherited-type")
    public MyBook findWithInheritedTypeSecurity(@PathParam("title") String title) {
        return inheritedTypeSecurityRepository.findWithInheritedTypeSecurity(title);
    }

    @GET
    @Path("/{title}/inherited-method")
    public MyBook findWithInheritedMethodSecurity(@PathParam("title") String title) {
        return inheritedMethodSecurityRepository.findWithInheritedMethodSecurity(title);
    }

    @GET
    @Path("/{title}/precedence")
    public MyBook findWithMethodLevelOverTypeLevel(@PathParam("title") String title) {
        return precedenceRepository.findWithMethodRoleOverTypeDenyAll(title);
    }

    @GET
    @Path("/{title}/override")
    public MyBook findWithOverriddenMethod(@PathParam("title") String title) {
        return inheritedMethodSecurityRepository.findWithOverriddenMethodSecurity(title);
    }
}
