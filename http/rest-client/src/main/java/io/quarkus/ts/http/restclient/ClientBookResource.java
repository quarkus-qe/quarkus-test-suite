package io.quarkus.ts.http.restclient;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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