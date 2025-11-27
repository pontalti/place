package com.demo.place.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Home", description = "Home endpoint")
@RestController
@RequestMapping(path = "/")
@RequiredArgsConstructor
public class HomeController {

	
    @Operation(summary = "Home", description = "place API home page")
    @ApiResponse(responseCode = "200", description = "Welcome message")
    @GetMapping
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("Place - code challenge - Home!");
    }
    
}
