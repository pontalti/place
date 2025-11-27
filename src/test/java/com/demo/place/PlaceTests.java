package com.demo.place;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StreamUtils;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK, classes = PlaceApplication.class)
@AutoConfigureMockMvc
public class PlaceTests {

	@Autowired
	private JsonMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;
      
    private static String readJsonFile(String fileName) throws IOException {
        return StreamUtils.copyToString(
                new ClassPathResource(fileName).getInputStream(),
                StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Test getAllplace endpoint")
    public void getAllplace() throws Exception {
        this.mockMvc.perform(get("/place"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test getPlaceById endpoint")
    public void getPlaceById() throws Exception {
        this.mockMvc.perform(get("/place/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test deleteById endpoint")
    public void deleteById() throws Exception {
        this.mockMvc.perform(delete("/place/2"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test createPlace endpoint")
    public void createPlace() throws Exception {
        var json = readJsonFile("place.json");

        this.mockMvc.perform(post("/place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test createPlace endpoint - bad request validation")
    public void createPlaceBadRequest() throws Exception {
        var json = readJsonFile("place_bad_request.json");

        this.mockMvc.perform(post("/place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test createPlace endpoint - bad request validation")
    public void createPlaceMalformed() throws Exception {
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
    public void createPlaceWrongTime(String fileName) throws Exception {
        var json = readJsonFile(fileName);

        this.mockMvc.perform(post("/place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test groupedOpeningHoursStructure endpoint")
    public void groupedOpeningHoursStructure() throws Exception {
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
            assertThat(group.get("day").isString()).isTrue();

            var hours = group.get("intervals");
            assertThat(hours).isNotNull();
            assertThat(hours.isArray() || hours.isString()).isTrue();
        }
    }

    @ParameterizedTest
    @DisplayName("Test updatePlace endpoint - full update")
    @CsvSource({"place.json,place_update.json"})
    public void updatePlace(String createFileName, String updateFileName) throws Exception {
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
        var updateNode = (ObjectNode) this.objectMapper.readTree(updateJsonRaw);

        updateNode.put("id", createdId);
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
    public void partialUpdatePlace(String createFileName, String partialUpdateFileName) throws Exception {
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
        var updateNode = (ObjectNode) this.objectMapper.readTree(updateJsonRaw);

        updateNode.put("id", createdId);
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