package com.demo.place.resource;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import com.demo.place.records.GroupedPlaceRecord;
import com.demo.place.records.PlacePatchRecord;
import com.demo.place.records.PlaceRecord;
import com.demo.place.service.GroupPlaceService;
import com.demo.place.service.PlaceService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

@Path("/place")
@ApplicationScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PlaceResource {

	private final PlaceService placeService;
	private final GroupPlaceService groupPlaceService;

	@POST
	@Path("")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Create multiple places", description = "Saves a list of validated places")
	@APIResponses({
			@APIResponse(responseCode = "201", description = "Place(s) created successfully", 
							content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PlaceRecord[].class))),
			@APIResponse(responseCode = "400", description = "Invalid or malformed request", content = @Content) })
	public Response savePlace(
			@RequestBody(required = true, 
							description = "List of PlaceRecord objects to create", 
							content = @Content(mediaType = MediaType.APPLICATION_JSON, 
							schema = @Schema(implementation = PlaceRecord[].class))) 
			@Valid @Size(min = 1, message = "Provide at least one location.") List<PlaceRecord> places) {
		List<PlaceRecord> savedPlaces = placeService.savePlace(places);
		return Response.status(Response.Status.CREATED).entity(savedPlaces).build();
	}

	@GET
	@Path("")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Lista todos os lugares", description = "Retorna a lista de lugares cadastrados")
	@APIResponse(responseCode = "200", description = "Lista de lugares", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PlaceRecord.class)))
	public Response fetchAll() {
		List<PlaceRecord> places = placeService.listAll();
		return Response.ok(places).build();
	}
	
	@GET
	@Path("/{id}")
	@Operation(summary = "Fetch a Place by ID", description = "Returns a place details")
    public Response findById(@PathParam("id") @Parameter(name = "id", description = "Place ID", example = "1", required = true) Long id) {
        var place = this.placeService.findById(id);
        return Response.ok(place).build();
    }

	@GET
	@Path("/{id}/opening-hours/grouped")
	@Operation(summary = "Fetch a Place by ID", description = "Returns a place details and its grouped opening hours")
	@APIResponses({
			@APIResponse(responseCode = "200", description = "Place found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GroupedPlaceRecord.class))),
			@APIResponse(responseCode = "404", description = "Place not found") })
	public Response fetchPlace(@PathParam("id") @Parameter(name = "id", description = "Place ID", example = "1", required = true) Long placeId) {
		GroupedPlaceRecord placeResponse = groupPlaceService.getGroupedOpeningHoursByPlaceId(placeId);
		return Response.ok(placeResponse).build();
	}

	@PUT
	@Path("")
	@Operation(summary = "Update a Place resource", description = "Updates all attributes of a Place resource identified by ID")
	@RequestBody(required = true, description = "Place payload for full update", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PlaceRecord.class)))
	@APIResponses({
			@APIResponse(responseCode = "200", description = "Place updated successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PlaceRecord.class))),
			@APIResponse(responseCode = "404", description = "Place not found"),
			@APIResponse(responseCode = "400", description = "Invalid or malformed request") })
	public Response updatePlace(@Valid PlaceRecord payload) {
		PlaceRecord updated = placeService.updatePlace(payload);
		return Response.ok(updated).build();
	}

	@PATCH
	@Path("")
	@Operation(summary = "Partially update a Place resource", description = """
                Updates only the attributes provided in the PATCH request body.
                
                **Validation Rules:**
                1. The `id` field is **mandatory** (required) in the request body.
                2. At least **one** update field (`label`, `location`, or `days`) must be supplied.
                
                **Update Behavior:**
                • Any field (excluding `id`) that is **omitted** from the JSON remains unchanged in the resource.
                • The `days` list (opening hours) is typically treated as a *sub-resource*; the supplied list will be processed by the service to merge or overwrite existing entries.
			""")
	@RequestBody(required = true, description = "Partial Place payload", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PlacePatchRecord.class)))
	@APIResponses({
			@APIResponse(responseCode = "200", description = "Place partially updated successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PlaceRecord.class))),
			@APIResponse(responseCode = "404", description = "Place not found"),
			@APIResponse(responseCode = "400", description = "Invalid request (validation failure or malformed payload)") })
	public Response patchPlace(@Valid PlacePatchRecord patchRecord) {
		PlaceRecord updated = placeService.patchPlace(patchRecord);
		return Response.ok(updated).build();
	}

	@DELETE
	@Path("/{id}")
	@Operation(summary = "Delete a Place resource", description = "Deletes a Place resource by ID")
	@APIResponses({ @APIResponse(responseCode = "204", description = "Place removed successfully"),
			@APIResponse(responseCode = "404", description = "Place not found") })
	public Response deletePlace(
			@Parameter(name = "id", description = "Place ID", example = "1", required = true) @PathParam("id") Long id) {
		this.placeService.deleteById(id);
		return Response.noContent().build();
	}
}
