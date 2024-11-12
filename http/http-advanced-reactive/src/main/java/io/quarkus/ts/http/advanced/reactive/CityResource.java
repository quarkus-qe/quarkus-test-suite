package io.quarkus.ts.http.advanced.reactive;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import io.quarkus.logging.Log;

@Path("/city")
public class CityResource {

    private static final String YAML_FILE_PATH = "payload.yaml";

    private static final Logger LOG = Logger.getLogger(CityResource.class);

    private final ObjectMapper objectMapper = new YAMLMapper();

    @Inject
    private CityListWrapperSerializer serializer;

    private List<City> cityList = new ArrayList<>();

    @GET
    @Produces(value = MediaType.APPLICATION_XML)
    public CityListDTO getCities() {
        LOG.info("Received request to getCities");

        if (cityList.isEmpty()) {
            cityList.add(new City("San Bernardino", "EEUU"));
            cityList.add(new City("Brno", "Czech Republic"));
            cityList.add(new City("Zaragoza", "Spain"));
        }
        return new CityListDTO(cityList);
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(value = MediaType.APPLICATION_XML)
    public String createCity(CityListDTO cityListDTO) {

        if (cityListDTO == null) {
            LOG.error("Error deserializing XML");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("<error>Invalid XML payload</error>")
                    .build().toString();
        }

        return serializer.toXML(cityListDTO);
    }

    @POST
    @Path("/dto")
    @Produces("application/yaml")
    @Consumes("application/yaml")
    public Response handleDTOYamlPostRequest(CityListDTO cityListDTO) {
        try {
            LOG.info("Received YAML payload: " + cityListDTO.toString());
            return Response.ok(cityListDTO).build();
        } catch (Exception e) {
            LOG.error("Error processing CityListDTO payload", e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error processing cityListDTO: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/getYamlFile")
    @Produces("application/yaml")
    public Response getYamlFile() {

        try {
            CityListDTO cityListDTO = readYamlFile(YAML_FILE_PATH);

            if (cityListDTO != null) {
                LOG.info("content ----! " + cityListDTO);
                return Response.ok(cityListDTO).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Error reading YAML file")
                        .type("text/plain")
                        .build();
            }
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error reading YAML file: " + e.getMessage())
                    .type("text/plain")
                    .build();
        }
    }

    private CityListDTO readYamlFile(String yamlFilePath) throws IOException {
        java.nio.file.Path path = Paths.get(yamlFilePath);
        if (!Files.exists(path)) {
            throw new IOException("YAML file not found: " + yamlFilePath);
        }

        try (InputStream inputStream = Files.newInputStream(path)) {
            Yaml yamlParser = new Yaml();
            Map<String, List<Map<String, String>>> yamlMap = yamlParser.load(inputStream);

            List<City> cities = yamlMap.get("cityList").stream()
                    .map(cityMap -> new City(cityMap.get("name"), cityMap.get("country")))
                    .collect(Collectors.toList());

            return new CityListDTO(cities);
        } catch (IOException e) {
            LOG.error("Error reading YAML file: {}", yamlFilePath, e);
            throw e;
        }
    }

    @GET
    @Path("/{imageName}")
    @Consumes("image/jpg")
    public Response getImage(@PathParam("imageName") String imageName) {
        try {
            java.nio.file.Path imagePath = Paths.get("wolf_moon_howling.jpg");
            if (Files.exists(imagePath)) {
                byte[] imageData = Files.readAllBytes(imagePath);
                Log.info("Image retrieval successful for {} " + imageName);
                return Response.ok(imageData, determineContentType(imageName)).build();
            } else {
                Log.info("Image not found: {} " + imageName);
                return Response.status(Response.Status.NOT_FOUND).entity("Image not found").build();
            }
        } catch (IOException e) {
            Log.error("Error reading image for {}", imageName, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error reading image").build();
        }
    }

    @POST
    @Path("/image")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] postFormReturnFile(@RestForm("image") @PartType("image/jpg") File image) throws IOException {
        return IOUtils.toByteArray(image.toURI());
    }

    private String determineContentType(String imageName) {
        if (imageName.endsWith(".png")) {
            return "image/png";
        } else if (imageName.endsWith(".jpg") || imageName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else {
            return "application/octet-stream";
        }
    }

}
