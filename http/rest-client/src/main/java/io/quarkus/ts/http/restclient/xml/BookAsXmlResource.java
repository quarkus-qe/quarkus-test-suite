package io.quarkus.ts.http.restclient.xml;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Produces(MediaType.APPLICATION_XML)
@Path("/book/xml")
public class BookAsXmlResource {

    @GET
    public String hello() {
        return "<book><title>Title in Xml</title></book>";
    }
}
