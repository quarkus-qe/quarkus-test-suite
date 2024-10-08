package io.quarkus.ts.http.advanced.reactive.brotli4j;

import java.util.HashMap;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/compression")
public class Brotli4JResource {

    public final static String DEFAULT_TEXT_PLAIN = "In life, you have to trust that every little bit helps. As you know," +
            " every small step forward counts." +
            " It's the accumulation of these efforts that ultimately leads to success." +
            " So, don't underestimate the power of persistence and determination in achieving your dreams";

    @Inject
    Brotli4JRestMock brotli4JRestMock;

    @GET
    @Path("/brotli/json")
    @Produces(MediaType.APPLICATION_JSON)
    public HashMap<String, Object> jsonHttpCompressionResponse() {
        return brotli4JRestMock.returnResponse(Brotli4JRestMock.ResponseType.JSON);
    }

    @GET
    @Path("/brotli/xml")
    @Produces(MediaType.APPLICATION_XML)
    public String xmlHttpCompressionResponse() {
        return brotli4JRestMock.returnResponse(Brotli4JRestMock.ResponseType.XML).get("xml").toString();
    }

    @POST
    @Path("/decompression")
    @Produces(MediaType.TEXT_PLAIN)
    public String decompressionHttpResponse(byte[] compressedData) {
        String decompressedData = new String(compressedData);
        return decompressedData;
    }

    @GET
    @Path("/default/text")
    @Produces(MediaType.TEXT_PLAIN)
    public String textPlainDefaultHttpCompressionResponse() {
        return DEFAULT_TEXT_PLAIN;
    }

    @GET
    @Path("/text")
    @Produces(MediaType.TEXT_PLAIN)
    public String textPlainHttpCompressionResponse() {
        return brotli4JRestMock.returnTextPlainResponse(Brotli4JRestMock.ResponseType.TEXT);
    }

    @GET
    @Path("/text/big")
    @Produces(MediaType.TEXT_PLAIN)
    public String textPlainBigHttpCompressionResponse() {
        return brotli4JRestMock.returnTextPlainResponse(Brotli4JRestMock.ResponseType.BIG_TEXT);
    }

}
