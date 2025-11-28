package com.demo.place;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
public class HomeResourceTest {
	
    @BeforeAll
    public static void setupBasePath() {
        String apiVersion = ConfigProvider.getConfig().getValue("api.version", String.class);
        RestAssured.basePath = "/api/" + apiVersion;
    }
	
    @Test
    @DisplayName("Test home endpoint")
    public void homeEndpoint() {
        given()
          .when().get("/")
          .then()
             .statusCode(200)
             .body(is("Hello from Quarkus REST"));
    }

}
