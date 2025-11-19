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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;

@QuarkusTest
class PlaceResourceTest {

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
    @DisplayName("Test home endpoint")
    void homeEndpoint() {
        given()
          .when().get("/")
          .then()
             .statusCode(200)
             .body(is("Hello from Quarkus REST"));
    }

    @Test
    @DisplayName("Test getAllplace endpoint")
    void getAllplace() {
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
    void getPlaceById() {
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
    void deleteById() {
        given()
        .when()
            .delete("/place/2")
        .then()
            .statusCode(204);
    }

    @Test
    @DisplayName("Test createPlace endpoint")
    void createPlace() throws IOException {
        String json = readJsonFile("place.json");

        given()
            .contentType(ContentType.JSON)
            .body(json)
        .when()
            .post("/place")
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("[0].id", notNullValue());
    }

    @Test
    @DisplayName("Test createPlace endpoint - bad request validation")
    void createPlaceBadRequest() throws IOException {
        String json = readJsonFile("place_bad_request.json");

        given()
            .contentType(ContentType.JSON)
            .body(json)
        .when()
            .post("/place")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON);
    }

    @Test
    @DisplayName("Test createPlace endpoint - malformed json")
    void createPlaceMalformed() throws IOException {
        String json = readJsonFile("place_malformed.json");

        given()
            .contentType(ContentType.JSON)
            .body(json)
        .when()
            .post("/place")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON);
    }

    @ParameterizedTest
    @DisplayName("Test createPlace endpoint - bad request validation for wrong time")
    @CsvSource({"place_wrong_time_1.json", "place_wrong_time_2.json", "place_wrong_time_3.json"})
    void createPlaceWrongTime(String fileName) throws IOException {
        String json = readJsonFile(fileName);

        given()
            .contentType(ContentType.JSON)
            .body(json)
        .when()
            .post("/place")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON);
    }

    @Test
    @DisplayName("Test groupedOpeningHoursStructure endpoint")
    void groupedOpeningHoursStructure() throws IOException {
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
    void updatePlace(String createFileName, String updateFileName) throws IOException {
        String createdJson = readJsonFile(createFileName);
        String postResult =
                given()
                    .contentType(ContentType.JSON)
                    .body(createdJson)
                .when()
                    .post("/place")
                .then()
                    .statusCode(201)
                    .contentType(ContentType.JSON)
                    .extract()
                    .asString();

        JsonNode arrayNode = objectMapper.readTree(postResult);
        JsonNode firstItem = arrayNode.get(0);
        long createdId = firstItem.get("id").asLong();

        String updateJsonRaw = readJsonFile(updateFileName);
        JsonNode updateNode = objectMapper.readTree(updateJsonRaw);
        ((com.fasterxml.jackson.databind.node.ObjectNode) updateNode)
                .put("id", createdId);

        String updatedJson = objectMapper.writeValueAsString(updateNode);

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
    void partialUpdatePlace(String createFileName, String partialUpdateFileName) throws IOException {
        String createdJson = readJsonFile(createFileName);
        String postResult =
                given()
                    .contentType(ContentType.JSON)
                    .body(createdJson)
                .when()
                    .post("/place")
                .then()
                    .statusCode(201)
                    .contentType(ContentType.JSON)
                    .extract()
                    .asString();

        JsonNode arrayNode = objectMapper.readTree(postResult);
        JsonNode firstItem = arrayNode.get(0);
        long createdId = firstItem.get("id").asLong();

        String updateJsonRaw = readJsonFile(partialUpdateFileName);

        given()
            .contentType(ContentType.JSON)
            .body(updateJsonRaw)
        .when()
            .patch("/place/" + createdId)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", is(Math.toIntExact(createdId)))
            .body("label", is("Stadio Giuseppe Meazza -> Patch"))
            .body("days", notNullValue())
            .body("days.size()", is(7));
    }
}
