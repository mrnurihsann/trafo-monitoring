package com.pln.trafo.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
public class AuthControllerTest {

    @Test
    public void testLoginEndpoint() {
        String loginRequest = """
            {
                "username": "admin",
                "password": "password123"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
            .when()
            .post("/auth/login")
            .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data.token", notNullValue());
    }

    @Test
    public void testInvalidLogin() {
        String loginRequest = """
            {
                "username": "invalid",
                "password": "invalid"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
            .when()
            .post("/auth/login")
            .then()
            .statusCode(401)
            .body("success", is(false));
    }

    @Test
    public void testLogoutEndpoint() {
        given()
            .when()
            .post("/auth/logout")
            .then()
            .statusCode(200)
            .body("success", is(true));
    }
}
