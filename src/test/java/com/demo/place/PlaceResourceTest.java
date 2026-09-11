package com.demo.place;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;

@QuarkusTest
public class PlaceResourceTest {
	
    @BeforeAll
    public static void setupBasePath() {
        String apiVersion = ConfigProvider.getConfig().getValue("api.version", String.class);
        RestAssured.basePath = "/api/" + apiVersion;
    }

    @Inject
    private ObjectMapper objectMapper;

    private String readJsonFile(String fileName) throws IOException {
        try (InputStream in = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(fileName)) {

            if (in == null) {
                throw new IOException("Resource not found: " + fileName);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    @DisplayName("Test getAllplace endpoint")
    public void getAllplace() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/place")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    @Test
    @DisplayName("Test getPlaceById endpoint")
    public void getPlaceById() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/place/1")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    @Test
    @DisplayName("Test deleteById endpoint")
    public void deleteById() {
        given()
        .when()
            .delete("/place/2")
        .then()
            .statusCode(204);
    }

    
    @ParameterizedTest
    @DisplayName("Test createPlace endpoint")
    @CsvSource({"place.json"})
    public void createPlace(String createFileName) throws IOException {
        var createdJson = readJsonFile(createFileName);
        var array = objectMapper.readTree(createdJson);
        var place = array.get(0).toString();

        given()
            .contentType(ContentType.JSON)
            .body(place)
        .when()
            .post("/place")
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("id", notNullValue());
    }
    
    @ParameterizedTest
    @DisplayName("Test createPlace batch endpoint")
    @CsvSource({"place.json"})
    public void createPlaceBatch(String createFileName) throws IOException {
    	var createdJson = readJsonFile(createFileName);
        var places = objectMapper.readTree(createdJson);

        given()
            .contentType(ContentType.JSON)
            .body(places.toString())
        .when()
            .post("/place/batch")
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("[0].id", notNullValue());
    }
/*
    @Test
    @DisplayName("Test createPlace endpoint - bad request validation")
    public void createPlaceBadRequest() throws IOException {
        String json = readJsonFile("place_bad_request.json");

        given()
            .contentType(ContentType.JSON)
            .body(json)
        .when()
            .post("/place")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Test createPlace endpoint - malformed json")
    public void createPlaceMalformed() throws IOException {

        String json = readJsonFile("place_malformed.json");

        given()
            .contentType(ContentType.JSON)
            .body(json)
        .when()
            .post("/place")
        .then()
            .statusCode(400);
    }

    @ParameterizedTest
    @DisplayName("Test createPlace endpoint - bad request validation for wrong time")
    @CsvSource({"place_wrong_time_1.json", "place_wrong_time_2.json", "place_wrong_time_3.json"})
    public void createPlaceWrongTime(String fileName) throws IOException {
        String json = readJsonFile(fileName);

        given()
            .contentType(ContentType.JSON)
            .body(json)
        .when()
            .post("/place")
        .then()
            .statusCode(400);
    }
*/
    @ParameterizedTest
    @DisplayName("Test createPlace endpoint - bad request validation")
    @CsvSource({"place_bad_request.json", "place_malformed.json", "place_wrong_time_1.json", "place_wrong_time_2.json", "place_wrong_time_3.json"})
    public void createPlaceBadRequest(String fileName) throws IOException {
    	String json = readJsonFile(fileName);
        given()
            .contentType(ContentType.JSON)
            .body(json)
        .when()
            .post("/place")
        .then()
            .statusCode(400)
            .header("Content-Length", "0");
    }

    @Test
    @DisplayName("Test groupedOpeningHoursStructure endpoint")
    public void groupedOpeningHoursStructure() throws IOException {
        String response =
            given()
                .accept(ContentType.JSON)
            .when()
                .get("/place/1/opening-hours/grouped")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("label", not(emptyOrNullString()))
                .body("location", not(emptyOrNullString()))
                .body("openingHours", notNullValue())
                .extract()
                .asString();

        JsonNode json = objectMapper.readTree(response);
        JsonNode openingHours = json.get("openingHours");

        assertTrue(openingHours.isArray(), "`openingHours` deve ser um array");

        for (JsonNode group : openingHours) {
            assertTrue(group.get("day").isTextual(), "`day` deve ser texto");

            JsonNode hours = group.get("intervals");
            assertNotNull(hours, "`intervals` não pode ser nulo");
            assertTrue(hours.isArray() || hours.isTextual(),
                       "`intervals` deve ser array ou texto");
        }
    }


    @ParameterizedTest
    @DisplayName("Test updatePlace endpoint - full update")
    @CsvSource({"place.json,place_update.json"})
    public void updatePlace(String createFileName, String updateFileName) throws IOException {
        var createdJson = readJsonFile(createFileName);
        var array = this.objectMapper.readTree(createdJson);
        var place = array.get(0).toString();
        String postResult =
                given()
                    .contentType(ContentType.JSON)
                    .body(place)
                .when()
                    .post("/place")
                .then()
                    .statusCode(201)
                    .contentType(ContentType.JSON)
                    .extract()
                    .asString();

        var node = this.objectMapper.readTree(postResult);
        var createdId = node.get("id").asLong();

        String updateJsonRaw = readJsonFile(updateFileName);
        JsonNode updateNode = this.objectMapper.readTree(updateJsonRaw);
        ((ObjectNode) updateNode).put("id", createdId);

        String updatedJson = this.objectMapper.writeValueAsString(updateNode);

        given()
            .contentType(ContentType.JSON)
            .body(updatedJson)
        .when()
            .put("/place")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", is(Math.toIntExact(createdId)))
            .body("label", is("Stadio Giuseppe Meazza"))
            .body("location", is("Piazzale Angelo Moratti, 20151 Milano MI, Itália"));
    }

    @ParameterizedTest
    @DisplayName("Test updatePlace endpoint - partial update")
    @CsvSource({"place.json,place_partial_update.json"})
    public void partialUpdatePlace(String createFileName, String partialUpdateFileName) throws IOException {
    	var createdJson = readJsonFile(createFileName);
        var array = this.objectMapper.readTree(createdJson);
        var place = array.get(0).toString();
        String postResult =
                given()
                    .contentType(ContentType.JSON)
                    .body(place)
                .when()
                    .post("/place")
                .then()
                    .statusCode(201)
                    .contentType(ContentType.JSON)
                    .extract()
                    .asString();

        var node = this.objectMapper.readTree(postResult);
        var createdId = node.get("id").asLong();

        String updateJsonRaw = readJsonFile(partialUpdateFileName);
        JsonNode updateNode = this.objectMapper.readTree(updateJsonRaw);
        ((ObjectNode) updateNode).put("id", createdId);
        String updatedJson = this.objectMapper.writeValueAsString(updateNode);

        given()
            .contentType(ContentType.JSON)
            .body(updatedJson)
        .when()
            .patch("/place")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", is(Math.toIntExact(createdId)))
            .body("label", is("Stadio Giuseppe Meazza -> Patch"))
            .body("days", notNullValue())
            .body("days.size()", is(7));
    }
}
