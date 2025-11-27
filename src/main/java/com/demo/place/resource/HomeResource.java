package com.demo.place.resource;

import org.eclipse.microprofile.openapi.annotations.Operation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import lombok.NoArgsConstructor;

@Path("/")
@ApplicationScoped
@NoArgsConstructor
public class HomeResource {

    @GET
    @Path("")
    @Operation(
            summary = "Home",
            description = "place API home page"
    )
    public Response home() {
        return Response.ok("Hello from Quarkus REST").build();
    }
	
}
