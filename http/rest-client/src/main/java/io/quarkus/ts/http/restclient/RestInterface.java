package io.quarkus.ts.http.restclient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.quarkus.ts.http.restclient.xml.Book;

@RegisterRestClient
@Path("/book")
@RegisterClientHeaders
public interface RestInterface {

    @GET
    @Path("/xml")
    @Produces(MediaType.APPLICATION_XML)
    Book getAsXml();

    @GET
    @Path("/json")
    @Produces(MediaType.APPLICATION_JSON)
    Book getAsJson();
}
