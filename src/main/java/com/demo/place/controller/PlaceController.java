package com.demo.place.controller;

import com.demo.place.records.GroupedPlaceRecord;
import com.demo.place.records.PlacePatchRecord;
import com.demo.place.records.PlaceRecord;
import com.demo.place.service.GroupPlaceService;
import com.demo.place.service.PlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Place", description = "Endpoints to manage places and fetch grouped opening hours")
@RestController
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;
    private final GroupPlaceService groupPlaceService;

    @Operation(summary = "Home", description = "place API home page")
    @ApiResponse(responseCode = "200", description = "Welcome message")
    @GetMapping(path = "/")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("Place - code challenge - Home!");
    }

    @Operation(
            summary = "Create multiple places",
            description = "Saves a list of validated places",
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "Place(s) created successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = PlaceRecord.class))
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid payload or empty list")
            }
    )
    @PostMapping(path = "/place", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PlaceRecord>> savePlace(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of PlaceRecord to be created",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PlaceRecord.class))
                    )
            )
            @RequestBody
            @Valid @Size(min = 1, message = "Provide at least one location.")
            List<PlaceRecord> places
    ) {
        var saved = this.placeService.savePlace(places);
        return ResponseEntity.ok(saved);
    }

    @Operation(
            summary = "List all places",
            responses = @ApiResponse(responseCode = "200", description = "Returns list of places")
    )
    @GetMapping(path = "/place", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PlaceRecord>> listAll() {
        var list = this.placeService.listAll();
        return ResponseEntity.ok(list);
    }

    @Operation(
            summary = "Get place by ID",
            parameters = {
                    @Parameter(name = "id", description = "Place ID", example = "1", required = true)
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "Place found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PlaceRecord.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Place not found")
            }
    )
    @GetMapping(path = "/place/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PlaceRecord> findById(
            @PathVariable("id") @NotNull Long id
    ) {
        var place = this.placeService.findById(id);
        return ResponseEntity.ok(place);
    }

    @Operation(
            summary = "Get grouped opening hours by place ID",
            parameters = @Parameter(name = "id", description = "Place ID", example = "1", required = true),
            responses = @ApiResponse(
                    responseCode = "200", description = "Returns grouped opening hours",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GroupedPlaceRecord.class)
                    )
            )
    )
    @GetMapping(path = "/place/{id}/opening-hours/grouped", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GroupedPlaceRecord> getGroupedOpeningHoursByPlaceId(
            @PathVariable("id") @NotNull Long id
    ) {
        var dto = this.groupPlaceService.getGroupedOpeningHoursByPlaceId(id);
        return ResponseEntity.ok(dto);
    }

    @Operation(
            summary = "Delete place by ID",
            parameters = @Parameter(name = "id", description = "Place ID", example = "1", required = true),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Place deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Place not found")
            }
    )
    @DeleteMapping(path = "/place/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> deleteById(
            @PathVariable("id") @NotNull Long id
    ) {
        this.placeService.deleteById(id);
        return ResponseEntity.ok("Place deleted successfully");
    }

    @Operation(
            summary = "Fully update an existing Place resource",
            description = "Replaces all attributes of a Place, including label, location, and opening hours.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Resource successfully updated",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "404", description = "Resource not found",
                            content = @Content),
                    @ApiResponse(responseCode = "400", description = "Invalid or malformed request",
                            content = @Content)
            }
    )
    @PutMapping(value = "/place", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PlaceRecord> updatePlace(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The Place resource with updated data",
                    required = true
            )
            @Valid @RequestBody PlaceRecord updatedPlace) {
        return ResponseEntity.ok(placeService.updatePlace(updatedPlace));
    }

    @Operation(
            summary = "Partially update a Place resource",
            description = """
                    Updates only the attributes provided in the PATCH request body.
                    
                    **Validation Rules:**
                    1. The `id` field is **mandatory** (required) in the request body.
                    2. At least **one** update field (`label`, `location`, or `days`) must be supplied.
                    
                    **Update Behavior:**
                    • Any field (excluding `id`) that is **omitted** from the JSON remains unchanged in the resource.
                    • The `days` list (opening hours) is typically treated as a *sub-resource*; the supplied list will be processed by the service to merge or overwrite existing entries.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Resource updated successfully",
                            content = @Content(schema = @Schema(implementation = PlaceRecord.class))),
                    @ApiResponse(responseCode = "404", description = "Resource not found",
                            content = @Content),
                    @ApiResponse(responseCode = "400", description = "Invalid request (validation failure or malformed payload)",
                            content = @Content)
            }
    )
    @PatchMapping(value = "/place",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PlaceRecord> patchPlace(@Valid @RequestBody PlacePatchRecord patch) {
        PlaceRecord updated = this.placeService.patchPlace(patch);
        return ResponseEntity.ok(updated);
    }
}