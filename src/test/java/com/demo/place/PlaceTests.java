package com.demo.place;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.StreamUtils;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK, classes = PlaceApplication.class)
@AutoConfigureMockMvc
public class PlaceTests {
	
	private static final String API_VERSION = "v1";
	private static final String BASE = "/api/"+API_VERSION+"/place";

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
        this.mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test getPlaceById endpoint")
    public void getPlaceById() throws Exception {
        this.mockMvc.perform(get(BASE+"/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test deleteById endpoint")
    public void deleteById() throws Exception {
        this.mockMvc.perform(delete(BASE + "/2"))
                	.andExpect(status().isNoContent());
    }

    @ParameterizedTest
    @DisplayName("Test createPlace batch endpoint")
    @CsvSource({"place.json"})
    public void createPlaceBatch(String createFileName) throws Exception {
    	var createdJson = readJsonFile(createFileName);
        var places = objectMapper.readTree(createdJson);
        this.mockMvc.perform(post(BASE+"/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(places.toString()))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @ParameterizedTest
    @DisplayName("Test createPlace endpoint")
    @CsvSource({"place.json"})
    public void createPlace(String createFileName) throws Exception {
        var createdJson = readJsonFile(createFileName);
        var array = objectMapper.readTree(createdJson);
        var place = array.get(0).toString();
        this.mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(place))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test createPlace endpoint - bad request validation")
    public void createPlaceBadRequest() throws Exception {
        var json = readJsonFile("place_bad_request.json");

        this.mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test createPlace endpoint - bad request validation")
    public void createPlaceMalformed() throws Exception {
        var json = readJsonFile("place_malformed.json");

        this.mockMvc.perform(post(BASE)
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

        this.mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test groupedOpeningHoursStructure endpoint")
    public void groupedOpeningHoursStructure() throws Exception {
        var response = mockMvc.perform(get(BASE+"/1/opening-hours/grouped")
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
            assertTrue(group.get("day").isString(), "`day` deve ser texto");

            var hours = group.get("intervals");
            assertNotNull(hours, "`intervals` não pode ser nulo");
            assertTrue(hours.isArray() || hours.isString(), "`intervals` deve ser array ou texto");
        }
    }

    @ParameterizedTest
    @DisplayName("Test updatePlace endpoint - full update")
    @CsvSource({"place.json,place_update.json"})
    public void updatePlace(String createFileName, String updateFileName) throws Exception {
        var createdJson = readJsonFile(createFileName);
        var array = objectMapper.readTree(createdJson);
        var place = array.get(0).toString();
        var postResult = mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(place))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        var node = this.objectMapper.readTree(postResult);

        var createdId = node.get("id").asLong();

        var updateJsonRaw = readJsonFile(updateFileName);
        var updateNode = (ObjectNode) this.objectMapper.readTree(updateJsonRaw);

        updateNode.put("id", createdId);
        var updatedJson = this.objectMapper.writeValueAsString(updateNode);

        this.mockMvc.perform(put(BASE)
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
        var array = objectMapper.readTree(createdJson);
        var place = array.get(0).toString();
        var postResult = mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(place))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        var node = this.objectMapper.readTree(postResult);

        var createdId = node.get("id").asLong();

        var updateJsonRaw = readJsonFile(partialUpdateFileName);
        var updateNode = (ObjectNode) this.objectMapper.readTree(updateJsonRaw);

        updateNode.put("id", createdId);
        var updatedJson = this.objectMapper.writeValueAsString(updateNode);

        this.mockMvc.perform(patch(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(createdId))
                .andExpect(jsonPath("$.label").value("Stadio Giuseppe Meazza -> Patch"))
                .andExpect(jsonPath("$.days").isArray())
                .andExpect(jsonPath("$.days", hasSize(7)));
    }
    
    @Test
    @DisplayName("Test listPaged endpoint - default page")
    public void listPagedDefaults() throws Exception {
        this.mockMvc.perform(get(BASE).param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    @Test
    @DisplayName("Test listPaged endpoint - honours the size parameter")
    public void listPagedRespectsSize() throws Exception {
        this.mockMvc.perform(get(BASE)
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.content", hasSize(lessThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("Test listPaged endpoint - sorting is applied across the whole table, not per page")
    public void listPagedSortsByLabel() throws Exception {
        var ascending = pageContent(get(BASE)
                .param("page", "0")
                .param("size", "50")
                .param("sort", "label,asc"));
 
        var descending = pageContent(get(BASE)
                .param("page", "0")
                .param("size", "50")
                .param("sort", "label,desc"));
 
        var labelsAsc = labelsOf(ascending);
 
        // Nothing to prove with a single row: both directions look identical,
        // so the assertions below would pass on an unsorted implementation too.
        if (labelsAsc.size() < 2) {
            return;
        }
 
        var expectedAsc = new ArrayList<>(labelsAsc);
        expectedAsc.sort(Comparator.naturalOrder());
        assertEquals(expectedAsc, labelsAsc, "the page should come back sorted by label");
 
        assertEquals(labelsAsc.reversed(), labelsOf(descending),
                "desc should return the same labels in the opposite order");
    }

    @Test
    @DisplayName("Test listPaged endpoint - page beyond the last one returns 200 with empty content")
    public void listPagedOutOfRange() throws Exception {
        this.mockMvc.perform(get(BASE)
                        .param("page", "9999")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @DisplayName("Test listPaged endpoint - the days of each place come back with the record")
    public void listPagedIncludesDays() throws Exception {
        var content = pageContent(get(BASE).param("page", "0").param("size", "5"));
        /*
        Guards against the N+1 fix regressing into a LazyInitializationException:
        the collection is fetched by the entity graph, and a missing or null
        "days" here means the mapping ran outside the transaction.
        */
        for (JsonNode place : content) {
        	assertNotNull(place.get("days"));
        	assertTrue(place.get("days").isArray());
        }
    }

    @Test
    @DisplayName("Test listPaged endpoint - without the page parameter the unpaginated listing answers")
    public void listAllStillReturnsAnArray() throws Exception {
        /*
    	The two handlers share the same path; `params = "page"` is the only
        thing routing between them, so this asserts the fallback still works.
        */
        this.mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    /** Runs the request and returns the {@code content} node of the page envelope. */
    private JsonNode pageContent(MockHttpServletRequestBuilder request) throws Exception {
        var response = this.mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return this.objectMapper.readTree(response).get("content");
    }

    private List<String> labelsOf(JsonNode content) {
        var labels = new ArrayList<String>();
        content.forEach(place -> labels.add(place.get("label").asString()));
        return labels;
    }
}