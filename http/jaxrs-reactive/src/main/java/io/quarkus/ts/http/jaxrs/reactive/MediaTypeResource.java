package io.quarkus.ts.http.jaxrs.reactive;

import static io.quarkus.ts.http.jaxrs.reactive.MediaTypeResource.MEDIA_TYPE_PATH;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_ATOM_XML;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_PATCH_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.MediaType.APPLICATION_SVG_XML;
import static javax.ws.rs.core.MediaType.APPLICATION_XHTML_XML;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_XML;
import static javax.ws.rs.core.MediaType.WILDCARD;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

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
