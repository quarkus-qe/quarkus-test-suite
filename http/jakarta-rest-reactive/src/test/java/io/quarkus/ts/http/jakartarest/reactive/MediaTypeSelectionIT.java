package io.quarkus.ts.http.jakartarest.reactive;

import static io.quarkus.ts.http.jakartarest.reactive.MediaTypeResource.GET_WITHOUT_CONSUMED_MEDIA_TYPES;
import static io.quarkus.ts.http.jakartarest.reactive.MediaTypeResource.MEDIA_TYPE_PATH;
import static io.quarkus.ts.http.jakartarest.reactive.MediaTypeResource.PATCH_APP_OCTET_STREAM;
import static io.quarkus.ts.http.jakartarest.reactive.MediaTypeResource.PATCH_WITHOUT_CONSUMED_MEDIA_TYPES;
import static io.quarkus.ts.http.jakartarest.reactive.MediaTypeResource.POST_APP_ATOM_XML_AND_TEXT_XML_EXT_AND_APP_JSON_PATCH_JSON;
import static io.quarkus.ts.http.jakartarest.reactive.MediaTypeResource.POST_APP_JSON_AND_TEXT_XML_AND_TEXT_PLAIN;
import static io.restassured.http.ContentType.BINARY;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.http.ContentType.MULTIPART;
import static io.restassured.http.ContentType.XML;
import static jakarta.ws.rs.core.MediaType.APPLICATION_ATOM_XML;
import static jakarta.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_PATCH_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_SVG_XML;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XHTML_XML;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML;
import static jakarta.ws.rs.core.MediaType.TEXT_XML;
import static jakarta.ws.rs.core.MediaType.WILDCARD;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.RestAssured;

@Tag("QUARKUS-2743")
@QuarkusScenario
public class MediaTypeSelectionIT {

    @Test
    public void testUnsupportedMediaType() {
        // no resource method consumes multipart/form-data nor wild card
        RestAssured.given()
                .contentType("multipart/form-data")
                .post(MEDIA_TYPE_PATH)
                .then()
                .statusCode(415);
        // no resource method consumes 'application/atom+*' and asterisk is treated as any other character after plus,
        // therefore 'application/atom+xml' is not matched
        RestAssured.given()
                .contentType("application/atom+*")
                .post(MEDIA_TYPE_PATH)
                .then()
                .statusCode(415);
        // 'application/atom+xml' is not matched
        RestAssured.given()
                .contentType("application/atom")
                .post(MEDIA_TYPE_PATH)
                .then()
                .statusCode(415);
        // 'text/xml' is not matched
        RestAssured.given()
                .contentType("text/xml-foo")
                .post(MEDIA_TYPE_PATH)
                .then()
                .statusCode(415);
    }

    @Test
    public void testExactMatchApplicationSoapXml() {
        RestAssured.given()
                .contentType("application/soap+xml")
                .post(MEDIA_TYPE_PATH)
                .then()
                .statusCode(200)
                .body(Matchers.equalTo("application/soap+xml"));
    }

    @Test
    public void testMediaSubTypesRanges() {
        // resource method 'postAppJsonTextXmlTextPlain' should match below calls as it
        // consumes 'application/xml', 'text/xml' and 'application/json'
        RestAssured.given()
                .contentType(APPLICATION_XML)
                .post(MEDIA_TYPE_PATH)
                .then()
                .statusCode(200)
                .body(Matchers.equalTo(POST_APP_JSON_AND_TEXT_XML_AND_TEXT_PLAIN));
        RestAssured.given()
                .contentType(TEXT_XML)
                .post(MEDIA_TYPE_PATH)
                .then()
                .statusCode(200)
                .body(Matchers.equalTo(POST_APP_JSON_AND_TEXT_XML_AND_TEXT_PLAIN));
        RestAssured.given()
                .contentType(APPLICATION_JSON)
                .post(MEDIA_TYPE_PATH)
                .then()
                .statusCode(200)
                .body(Matchers.equalTo(POST_APP_JSON_AND_TEXT_XML_AND_TEXT_PLAIN));
        // resource method 'postAppAtomXmlAndTextAnyXmlExtAndAppJsonPatch' should match below calls as it
        // consumes 'application/atom+xml', 'text/xml-external-parsed-entity' and 'application/json-patch+json';
        // media types consumed by resource method 'postAppAtomXmlAndTextAnyXmlExtAndAppJsonPatch' are more specific
        // than media types consumed by 'postAppJsonTextXmlTextPlain' and the most specific reference is selected
        RestAssured.given()
                .contentType(APPLICATION_ATOM_XML)
                .post(MEDIA_TYPE_PATH)
                .then()
                .statusCode(200)
                .body(Matchers.equalTo(POST_APP_ATOM_XML_AND_TEXT_XML_EXT_AND_APP_JSON_PATCH_JSON));
        RestAssured.given()
                .contentType("text/xml-external-parsed-entity")
                .post(MEDIA_TYPE_PATH)
                .then()
                .statusCode(200)
                .body(Matchers.equalTo(POST_APP_ATOM_XML_AND_TEXT_XML_EXT_AND_APP_JSON_PATCH_JSON));
        RestAssured.given()
                .contentType(APPLICATION_JSON_PATCH_JSON)
                .post(MEDIA_TYPE_PATH)
                .then()
                .statusCode(200)
                .body(Matchers.equalTo(POST_APP_ATOM_XML_AND_TEXT_XML_EXT_AND_APP_JSON_PATCH_JSON));
    }

    @Test
    public void testNoMediaType() {
        // resource method without consumes has priority over wildcard
        RestAssured.given()
                .contentType(APPLICATION_JSON)
                .get(MEDIA_TYPE_PATH)
                .then()
                .statusCode(200)
                .body(Matchers.equalTo(GET_WITHOUT_CONSUMED_MEDIA_TYPES));
        RestAssured.given()
                .contentType(APPLICATION_SVG_XML)
                .get(MEDIA_TYPE_PATH)
                .then()
                .statusCode(200)
                .body(Matchers.equalTo(GET_WITHOUT_CONSUMED_MEDIA_TYPES));
    }

    @Test
    public void testAcceptHeaders() {
        // here we accept one of xml types, therefore resource method producing application/xhtml+xml is matched
        RestAssured.given()
                .contentType(APPLICATION_JSON)
                .accept(XML)
                .patch(MEDIA_TYPE_PATH)
                .then()
                .statusCode(200)
                .body(Matchers.equalTo(PATCH_WITHOUT_CONSUMED_MEDIA_TYPES));
        // here we accept one of json types, but no endpoint matches, therefore method producing wildcard is used
        RestAssured.given()
                .contentType(APPLICATION_SVG_XML)
                .accept(JSON)
                .patch(MEDIA_TYPE_PATH)
                .then()
                .statusCode(200)
                .body(Matchers.equalTo(APPLICATION_SVG_XML));
        // here we accept multipart, but no endpoint matches, therefore method producing wildcard is used
        RestAssured.given()
                .contentType(APPLICATION_SVG_XML)
                .accept(MULTIPART)
                .patch(MEDIA_TYPE_PATH)
                .then()
                .statusCode(200)
                .body(Matchers.equalTo(APPLICATION_SVG_XML));
        // exact match for accept header has priority over wildcard
        RestAssured.given()
                .contentType(APPLICATION_SVG_XML)
                .accept(BINARY)
                .patch(MEDIA_TYPE_PATH)
                .then()
                .statusCode(200)
                .body(Matchers.equalTo(PATCH_APP_OCTET_STREAM));
    }

    @Test
    public void testWildCardMediaType() {
        // test wildcard is fallback choice when no other resource method matches
        RestAssured.given()
                .contentType(APPLICATION_XHTML_XML)
                .put(MEDIA_TYPE_PATH)
                .then()
                .statusCode(200)
                .body(Matchers.equalTo(WILDCARD));
    }

    @Test
    public void testMediaSubTypeWildcard() {
        // matches '*/*' as that's the only HTTP PUT method
        RestAssured.given()
                .contentType("application/*")
                .put(MEDIA_TYPE_PATH)
                .then()
                .statusCode(200)
                .body(Matchers.equalTo(WILDCARD));
        // matches resource method without defined content type as 'application/atom+*' does not match any type
        RestAssured.given()
                .contentType("application/atom+*")
                .patch(MEDIA_TYPE_PATH)
                .then()
                .statusCode(200)
                .body(Matchers.equalTo(PATCH_WITHOUT_CONSUMED_MEDIA_TYPES));
    }

    @Test
    public void testQualityDeterminesFormUrlEncoded() {
        RestAssured.given()
                .contentType(APPLICATION_SVG_XML)
                .accept(String.format("%s;q=0.4, %s;q=0.5", APPLICATION_SVG_XML, APPLICATION_FORM_URLENCODED))
                .patch(MEDIA_TYPE_PATH)
                .then()
                .statusCode(200)
                .body(Matchers.equalTo(APPLICATION_FORM_URLENCODED));
    }

}
