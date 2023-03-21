package io.quarkus.ts.http.jakartarest.reactive;

import static io.quarkus.ts.http.jakartarest.reactive.MediaTypeResource.MEDIA_TYPE_PATH;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_ATOM_XML;
import static jakarta.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_PATCH_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static jakarta.ws.rs.core.MediaType.APPLICATION_SVG_XML;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XHTML_XML;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML;
import static jakarta.ws.rs.core.MediaType.TEXT_XML;
import static jakarta.ws.rs.core.MediaType.WILDCARD;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

@Path(MEDIA_TYPE_PATH)
public class MediaTypeResource {

    public static final String MEDIA_TYPE_PATH = "/media-type";
    public static final String POST_APP_JSON_AND_TEXT_XML_AND_TEXT_PLAIN = "postAppJsonTextXmlTextPlain";
    public static final String POST_APP_ATOM_XML_AND_TEXT_XML_EXT_AND_APP_JSON_PATCH_JSON = "postAppAtomXmlAndTextAnyXmlExtAndAppJsonPatch";
    public static final String GET_WITHOUT_CONSUMED_MEDIA_TYPES = "getWithoutConsumedMediaTypes";
    public static final String PATCH_WITHOUT_CONSUMED_MEDIA_TYPES = "patchWithoutConsumedMediaTypes";
    public static final String PATCH_APP_OCTET_STREAM = "patchAppOctetStream";

    @Consumes({ "application/soap+xml" })
    @POST
    public Response postApplicationSoapXml() {
        return Response.ok("application/soap+xml").build();
    }

    @Consumes({ "custom/media-type" })
    @POST
    public Response postCustomMediaType(@HeaderParam(CONTENT_TYPE) String contentType) {
        return Response.ok(contentType).build();
    }

    @Consumes({ APPLICATION_XML, TEXT_XML, APPLICATION_JSON })
    @POST
    public Response postAppJsonTextXmlTextPlain() {
        return Response.ok(POST_APP_JSON_AND_TEXT_XML_AND_TEXT_PLAIN).build();
    }

    @Consumes({ APPLICATION_ATOM_XML, "text/xml-external-parsed-entity", APPLICATION_JSON_PATCH_JSON })
    @POST
    public Response postAppAtomXmlAndTextAnyXmlExtAndAppJsonPatch() {
        return Response.ok(POST_APP_ATOM_XML_AND_TEXT_XML_EXT_AND_APP_JSON_PATCH_JSON).build();
    }

    @GET
    public Response getWithoutConsumedMediaTypes() {
        return Response.ok(GET_WITHOUT_CONSUMED_MEDIA_TYPES).build();
    }

    @Consumes(WILDCARD)
    @GET
    public Response getWildcard() {
        // this endpoint is never reached as getWithoutConsumedMediaTypes has priority over wildcard
        return Response.ok(WILDCARD).build();
    }

    @Produces(APPLICATION_XHTML_XML)
    @PATCH
    public Response patchWithoutConsumedMediaTypes() {
        return Response.ok(PATCH_WITHOUT_CONSUMED_MEDIA_TYPES).build();
    }

    @Produces(WILDCARD)
    @Consumes(APPLICATION_SVG_XML)
    @PATCH
    public Response patchWildcard() {
        return Response.ok(APPLICATION_SVG_XML).build();
    }

    @Produces(APPLICATION_OCTET_STREAM)
    @Consumes(APPLICATION_SVG_XML)
    @PATCH
    public Response patchAppOctetStream() {
        return Response.ok(PATCH_APP_OCTET_STREAM).build();
    }

    @Produces(APPLICATION_FORM_URLENCODED)
    @Consumes(APPLICATION_SVG_XML)
    @PATCH
    public Response patchAppFormUrlEncoded() {
        return Response.ok(APPLICATION_FORM_URLENCODED).build();
    }

    @Produces(APPLICATION_SVG_XML)
    @Consumes(APPLICATION_SVG_XML)
    @PATCH
    public Response patchAppSvgXml() {
        return Response.ok(APPLICATION_SVG_XML).build();
    }

    @Consumes(WILDCARD)
    @PUT
    public Response putWildcard() {
        return Response.ok(WILDCARD).build();
    }
}
