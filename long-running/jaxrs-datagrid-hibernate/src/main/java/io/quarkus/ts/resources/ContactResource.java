package io.quarkus.ts.resources;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.quarkus.ts.model.Contact;

@Path("/contacts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContactResource {

    private static Map<Long, Contact> contactsRepository = new HashMap<>();

    @POST
    public Response createContact(Contact contact) {
        Response.ResponseBuilder builder;
        Long nextId = contactsRepository.keySet().size() + 1L;
        try {
            contact.setId(nextId);
            contactsRepository.put(nextId, contact);

            builder = Response.ok(contact);
        } catch (Exception e) {
            builder = Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage());
        }

        return builder.build();
    }

    @DELETE
    public Response removeAllContacts() {
        contactsRepository.clear();
        return Response.ok().build();
    }

    @DELETE
    @Path("/{id}")
    public Response removeContact(final @PathParam("id") Long id) {
        contactsRepository.remove(id);
        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        Collection<Contact> allContacts = contactsRepository.values();
        return Response.ok(allContacts).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(final @PathParam("id") Long id) {
        Contact contact = contactsRepository.get(id);
        return Response.ok(contact).build();
    }
}
