package com.demo.place;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK, classes = PlaceApplication.class)
@AutoConfigureMockMvc
class PlaceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String readJsonFile(String fileName) throws IOException {
        return StreamUtils.copyToString(
                new ClassPathResource(fileName).getInputStream(),
                StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Test home endpoint")
    void homeEndpoint() throws Exception {
        this.mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string("Place - code challenge - Home!"));
    }

    @Test
    @DisplayName("Test getAllplace endpoint")
    void getAllplace() throws Exception {
        this.mockMvc.perform(get("/place"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test getPlaceById endpoint")
    void getPlaceById() throws Exception {
        this.mockMvc.perform(get("/place/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test deleteById endpoint")
    void deleteById() throws Exception {
        this.mockMvc.perform(delete("/place/2"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test createPlace endpoint")
    void createPlace() throws Exception {
        var json = readJsonFile("place.json");

        this.mockMvc.perform(post("/place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test createPlace endpoint - bad request validation")
    void createPlaceBadRequest() throws Exception {
        var json = readJsonFile("place_bad_request.json");

        this.mockMvc.perform(post("/place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test createPlace endpoint - bad request validation")
    void createPlaceMalformed() throws Exception {
        var json = readJsonFile("place_malformed.json");

        this.mockMvc.perform(post("/place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @ParameterizedTest
    @DisplayName("Test createPlace endpoint - bad request validation for wrong time")
    @CsvSource({"place_wrong_time_1.json", "place_wrong_time_2.json", "place_wrong_time_3.json"})
    void createPlaceWrongTime(String fileName) throws Exception {
        var json = readJsonFile(fileName);

        this.mockMvc.perform(post("/place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test groupedOpeningHoursStructure endpoint")
    void groupedOpeningHoursStructure() throws Exception {
        var response = mockMvc.perform(get("/place/1/opening-hours/grouped")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.label").isNotEmpty())
                .andExpect(jsonPath("$.location").isNotEmpty())
                .andExpect(jsonPath("$.openingHours").isArray())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var json = this.objectMapper.readTree(response);
        var openingHours = json.get("openingHours");

        for (JsonNode group : openingHours) {
            assertThat(group.get("day").isTextual()).isTrue();

            var hours = group.get("intervals");
            assertThat(hours).isNotNull();
            assertThat(hours.isArray() || hours.isTextual()).isTrue();
        }
    }

    @ParameterizedTest
    @DisplayName("Test updatePlace endpoint - full update")
    @CsvSource({"place.json,place_update.json"})
    void updatePlace(String createFileName, String updateFileName) throws Exception {
        var createdJson = readJsonFile(createFileName);
        var postResult = mockMvc.perform(post("/place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createdJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        var arrayNode = this.objectMapper.readTree(postResult);

        var firstItem = arrayNode.get(0);
        var createdId = firstItem.get("id").asLong();

        var updateJsonRaw = readJsonFile(updateFileName);
        var updateNode = this.objectMapper.readTree(updateJsonRaw);

        ((com.fasterxml.jackson.databind.node.ObjectNode) updateNode).put("id", createdId);
        var updatedJson = this.objectMapper.writeValueAsString(updateNode);

        this.mockMvc.perform(put("/place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(createdId))
                .andExpect(jsonPath("$.label").value("Stadio Giuseppe Meazza"))
                .andExpect(jsonPath("$.location").value("Piazzale Angelo Moratti, 20151 Milano MI, Itália"));
    }

    @ParameterizedTest
    @DisplayName("Test updatePlace endpoint - full update")
    @CsvSource({"place.json,place_partial_update.json"})
    void partialUpdatePlace(String createFileName, String partialUpdateFileName) throws Exception {
        var createdJson = readJsonFile(createFileName);
        var postResult = mockMvc.perform(post("/place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createdJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        var arrayNode = this.objectMapper.readTree(postResult);

        var firstItem = arrayNode.get(0);
        var createdId = firstItem.get("id").asLong();

        var updateJsonRaw = readJsonFile(partialUpdateFileName);
        var updateNode = this.objectMapper.readTree(updateJsonRaw);

        ((com.fasterxml.jackson.databind.node.ObjectNode) updateNode).put("id", createdId);
        var updatedJson = this.objectMapper.writeValueAsString(updateNode);

        this.mockMvc.perform(patch("/place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(createdId))
                .andExpect(jsonPath("$.label").value("Stadio Giuseppe Meazza -> Patch"))
                .andExpect(jsonPath("$.days").isArray())
                .andExpect(jsonPath("$.days", hasSize(7)));
    }
}