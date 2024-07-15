package io.quarkus.ts.http.advanced.reactive;

import static io.restassured.config.EncoderConfig.encoderConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.core.MediaType;
import jakarta.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.hamcrest.CoreMatchers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.RestAssured;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.MultiPartSpecification;

@QuarkusScenario
public class NonJsonPayLoadIT {
    @QuarkusApplication(classes = { YamlProvider.class, CityResource.class, City.class, CityListDTO.class,
            CityListWrapper.class,
            CityListWrapperSerializer.class,
    }, properties = "oidcdisable.properties")
    static RestService app = new RestService();

    private static File imageFile;
    private static final String IMAGE = "image";

    private static byte[] imageBytes;
    private static final String EXPECTED_XML_PAYLOAD = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            " <cityListDTO> <cityList> <name>Paris</name>" +
            " <country>France</country>" +
            " </cityList> </cityListDTO>";

    private final static String EXPECTED_RESPONSE_BODY_YAML = "--- cityList: - name: \"Tokio\" country: \"Japan\" - name: \"Paris\" country: \"France\"";

    @Test
    public void testExpectedXmlResponse() {
        Response response = app.given()
                .get("/city").then()
                .statusCode(HttpStatus.SC_OK)
                .contentType(MediaType.APPLICATION_XML).extract().response();
        assertThat(response.asString(), containsStringIgnoringCase("Brno"));

    }

    @Test
    public void testXMLPayloadPostRequest() throws JAXBException {

        City city = new City("Paris", "France");
        List<City> cityList = new ArrayList<>();
        cityList.add(city);

        CityListDTO requestCityList = new CityListDTO(cityList);

        String payload = app.given().contentType(MediaType.APPLICATION_XML)
                .body(requestCityList)
                .when()
                .post("/city")
                .then()
                .extract().asString();
        Document doc = Jsoup.parse(payload);
        String unescapedXml = doc.text();
        assertThat(unescapedXml, equalTo(EXPECTED_XML_PAYLOAD));
    }

    @Test
    public void testExpectedYamlResponse() {

        final Response response = app.given()
                .contentType("application/yaml")
                .get("/city/getYamlFile")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().response();
        Document doc = Jsoup.parse(response.asPrettyString());
        String unescapedYaml = doc.text();
        assertThat(unescapedYaml, equalTo(EXPECTED_RESPONSE_BODY_YAML));

    }

    @Test
    public void testPostYamlPayloadRequest() throws IOException {

        String yamlString = readYamlFile("src/test/resources/payload.yaml");

        Response response = app
                .given()
                .log().all().filter(new ResponseLoggingFilter())
                .config(RestAssured.config()
                        .encoderConfig(encoderConfig().encodeContentTypeAs("application/yaml", ContentType.TEXT)))
                .contentType("application/yaml")
                .body(yamlString)
                .when()
                .post("/city/dto")
                .then()
                .log().all()
                .statusCode(200).extract().response();
        Yaml yaml = new Yaml();
        Map<String, Object> responseObject = yaml.load(response.asString());
        Assertions.assertEquals("[{name=Tokio, country=Japan}, {name=Paris, country=France}]",
                responseObject.get("cityList").toString());
    }

    @Test
    public void testGetImage() throws IOException {
        imageBytes = IOUtils.toByteArray(NonJsonPayLoadIT.class.getResourceAsStream("/wolf_moon_howling.jpg"));

        byte[] receivedBytes = app.given()
                .contentType("image/jpg")
                .get("/city/wolf_moon_howling.jpg")
                .then()
                .extract().asByteArray();

        assertThat(receivedBytes, CoreMatchers.equalTo(imageBytes));
    }

    @Test
    public void testImagePartFromMultipart() throws IOException {
        imageFile = new File(NonJsonPayLoadIT.class.getResource("/wolf_moon_howling.jpg").getFile());
        imageBytes = IOUtils.toByteArray(NonJsonPayLoadIT.class.getResourceAsStream("/wolf_moon_howling.jpg"));
        byte[] receivedBytes = postWithMultiPart("/city/image")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .extract().asByteArray();
        assertThat(receivedBytes, CoreMatchers.equalTo(imageBytes));
    }

    private static ValidatableResponse postWithMultiPart(String path) {
        MultiPartSpecification imageSpec = createImageSpec();

        return RestAssured.given()
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .multiPart(imageSpec)
                .post(path)
                .then()
                .statusCode(200);
    }

    private static MultiPartSpecification createImageSpec() {
        return new MultiPartSpecBuilder(imageFile)
                .controlName(IMAGE)
                .mimeType("image/png")
                .build();
    }

    private String readYamlFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return new String(Files.readAllBytes(path));
    }

}
