package io.quarkus.ts.http.restclient;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.ts.http.restclient.xml.Book;

@Path("/client/book")
public class ClientBookResource {

    @Inject
    @RestClient
    RestInterface restInterface;

    @GET
    @Path("/xml")
    @Produces(MediaType.APPLICATION_XML)
    public Book getAsXml() {
        return restInterface.getAsXml();
    }

    @GET
    @Path("/json")
    @Produces(MediaType.APPLICATION_JSON)
    public Book getAsJson() {
        return restInterface.getAsJson();
    }
}