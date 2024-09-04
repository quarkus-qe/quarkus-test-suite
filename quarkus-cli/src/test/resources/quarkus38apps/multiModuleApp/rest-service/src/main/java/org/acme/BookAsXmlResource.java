package org.acme;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Produces(MediaType.APPLICATION_XML)
@Path("/book/xml")
public class BookAsXmlResource {

    @GET
    public String hello() {
        return "<book><title>Title in Xml</title></book>";
    }
}
