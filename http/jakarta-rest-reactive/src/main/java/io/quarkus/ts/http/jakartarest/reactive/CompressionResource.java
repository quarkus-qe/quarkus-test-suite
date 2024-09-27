package io.quarkus.ts.http.jakartarest.reactive;

import java.util.Random;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestQuery;

import io.quarkus.vertx.http.Compressed;
import io.quarkus.vertx.http.Uncompressed;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;

@Path("/compression")
public class CompressionResource {
    public static String SMALL_MESSAGE = "small msg!";
    private static final Logger LOG = Logger.getLogger(CompressionResource.class);

    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/small/default_compression_text_plain")
    public Uni<String> smallDefaultCompressionTextPlain() {
        return defaultSmallResponse();
    }

    @GET
    @Consumes(MediaType.TEXT_HTML)
    @Produces(MediaType.TEXT_HTML)
    @Path("/small/default_compression_text_html")
    public Uni<String> smallDefaultCompressionTextHtml() {
        return defaultSmallResponse();
    }

    @GET
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    @Path("/small/default_compression_text_xml")
    public Uni<String> smallDefaultCompressionTextXml() {
        return defaultSmallResponse();
    }

    @GET
    @Consumes("text/css")
    @Produces("text/css")
    @Path("/small/default_compression_text_css")
    public Uni<String> smallDefaultCompressionTextCss() {
        return defaultSmallResponse();
    }

    @GET
    @Consumes("text/javascript")
    @Produces("text/javascript")
    @Path("/small/default_compression_text_js")
    public Uni<String> smallDefaultCompressionTextJS() {
        return defaultSmallResponse();
    }

    @GET
    @Consumes("application/javascript")
    @Produces("application/javascript")
    @Path("/small/default_compression_app_js")
    public Uni<String> smallDefaultCompressionAppJS() {
        return defaultSmallResponse();
    }

    @GET
    @Compressed
    @Consumes("application/x-custom-type")
    @Produces("application/x-custom-type")
    @Path("/small/compression_custom_type")
    public Uni<String> smallCompressionCustomType() {
        return defaultSmallResponse();
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/small/default_compression_json")
    public Uni<String> defaultCompressionJson() {
        return defaultSmallResponse();
    }

    @GET
    @Consumes(MediaType.APPLICATION_XHTML_XML)
    @Produces(MediaType.APPLICATION_XHTML_XML)
    @Path("/small/default_compression_xhtml_xml")
    public Uni<String> defaultCompressionXhtmlXml() {
        return defaultSmallResponse();
    }

    @GET
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("/small/default_no_compression_xml")
    public Uni<String> defaultNoCompressionXml() {
        return defaultSmallResponse();
    }

    @GET
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.MULTIPART_FORM_DATA)
    @Path("/small/default_no_compression_form_data")
    public Uni<String> defaultNoCompressionFormData() {
        return defaultSmallResponse();
    }

    @GET
    @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN })
    @Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN })
    @Path("/small/mixing_types")
    public Uni<String> mixingContentTypes() {
        return defaultSmallResponse();
    }

    @GET
    @Uncompressed
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/small/force_no_compression")
    public Uni<String> forceNoCompression(RoutingContext rc) {
        return defaultSmallResponse();
    }

    @GET
    @Path("/big/payload")
    @Compressed
    public Uni<byte[]> bigCompression(@RestQuery("bodyCharSize") String bodyCharSize) {
        byte[] payload = new byte[Integer.valueOf(bodyCharSize)];
        new Random().nextBytes(payload);
        LOG.info("Big compression payload generated.");
        return Uni.createFrom().item(payload);
    }

    private Uni<String> defaultSmallResponse() {
        return Uni.createFrom().item(SMALL_MESSAGE);
    }
}
