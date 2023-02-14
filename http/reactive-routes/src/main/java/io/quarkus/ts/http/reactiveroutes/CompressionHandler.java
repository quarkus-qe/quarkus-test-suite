package io.quarkus.ts.http.reactiveroutes;

import jakarta.ws.rs.core.MediaType;

import org.jboss.logging.Logger;

import io.quarkus.vertx.http.Compressed;
import io.quarkus.vertx.http.Uncompressed;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.vertx.ext.web.RoutingContext;

@RouteBase(path = "/compression/small")
public class CompressionHandler {

    public static String SMALL_MESSAGE = "small msg!";
    private static final Logger LOG = Logger.getLogger(CompressionHandler.class);

    @Route(path = "/default_compression_text_plain", consumes = { MediaType.TEXT_PLAIN }, produces = {
            MediaType.TEXT_PLAIN })
    public void smallDefaultCompressionTextPlain(RoutingContext rc) {
        defaultSmallResponse(rc);
    }

    @Route(path = "/default_compression_text_html", consumes = { MediaType.TEXT_HTML }, produces = {
            MediaType.TEXT_HTML })
    public void smallDefaultCompressionTextHtml(RoutingContext rc) {
        defaultSmallResponse(rc);
    }

    @Route(path = "default_compression_text_xml", consumes = { MediaType.TEXT_XML }, produces = { MediaType.TEXT_XML })
    void smallDefaultCompressionTextXml(RoutingContext rc) {
        defaultSmallResponse(rc);
    }

    @Route(path = "/default_compression_text_css", consumes = { "text/css" }, produces = { "text/css" })
    void smallDefaultCompressionTextCss(RoutingContext rc) {
        defaultSmallResponse(rc);
    }

    @Route(path = "/default_compression_text_js", consumes = { "text/javascript" }, produces = { "text/javascript" })
    void smallDefaultCompressionTextJS(RoutingContext rc) {
        defaultSmallResponse(rc);
    }

    @Route(path = "/default_compression_app_js", consumes = { "application/javascript" }, produces = {
            "application/javascript" })
    void smallDefaultCompressionAppJS(RoutingContext rc) {
        defaultSmallResponse(rc);
    }

    @Compressed
    @Route(path = "/compression_custom_type", consumes = { "application/x-custom-type" }, produces = {
            "application/x-custom-type" })
    public void smallCompressionCustomType(RoutingContext rc) {
        defaultSmallResponse(rc);
    }

    @Route(path = "/default_no_compression_json", consumes = { MediaType.APPLICATION_JSON }, produces = {
            MediaType.APPLICATION_JSON })
    public void defaultNoCompressionJson(RoutingContext rc) {
        defaultSmallResponse(rc);
    }

    @Route(path = "/default_no_compression_xml", consumes = { MediaType.APPLICATION_XML }, produces = {
            MediaType.APPLICATION_XML })
    public void defaultNoCompressionXml(RoutingContext rc) {
        defaultSmallResponse(rc);
    }

    @Route(path = "/default_no_compression_form_data", consumes = { MediaType.MULTIPART_FORM_DATA }, produces = {
            MediaType.MULTIPART_FORM_DATA })
    public void defaultNoCompressionFormData(RoutingContext rc) {
        defaultSmallResponse(rc);
    }

    @Route(path = "/mixing_types", consumes = { MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN }, produces = {
            MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    public void mixingContentTypes(RoutingContext rc) {
        defaultSmallResponse(rc);
    }

    @Uncompressed
    @Route(path = "/force_no_compression", consumes = { MediaType.TEXT_PLAIN }, produces = { MediaType.TEXT_PLAIN })
    public void forceNoCompression(RoutingContext rc) {
        defaultSmallResponse(rc);
    }

    private void defaultSmallResponse(RoutingContext rc) {
        rc.response().setStatusCode(200).end(SMALL_MESSAGE);
    }
}
