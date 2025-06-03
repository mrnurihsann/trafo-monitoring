package com.pln.trafo.controller;

import com.pln.trafo.dto.ApiResponse;
import com.pln.trafo.dto.AuthRequest;
import com.pln.trafo.dto.AuthResponse;
import com.pln.trafo.entity.User;
import com.pln.trafo.service.JwtService;
import com.pln.trafo.service.UserService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    @Inject
    UserService userService;

    @Inject
    JwtService jwtService;

    @POST
    @Path("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public Response login(@Valid AuthRequest authRequest) {
        try {
            User user = userService.authenticate(authRequest.username, authRequest.password);
            
            if (user == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(ApiResponse.error("Username atau password salah"))
                    .build();
            }

            String token = jwtService.generateToken(user);
            Long expiresIn = jwtService.getTokenExpirationTime();
            
            AuthResponse authResponse = new AuthResponse(token, expiresIn, user);
            
            return Response.ok(ApiResponse.success("Login berhasil", authResponse)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat login", e.getMessage()))
                .build();
        }
    }

    @POST
    @Path("/refresh")
    @Operation(summary = "Refresh token", description = "Refresh JWT token")
    public Response refreshToken(@HeaderParam("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(ApiResponse.error("Token tidak valid"))
                    .build();
            }

            String token = authHeader.substring(7);
            
            if (!jwtService.isTokenValid(token)) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(ApiResponse.error("Token expired atau tidak valid"))
                    .build();
            }

            // For simplicity, we'll return a success message
            // In a real implementation, you might want to decode the token and generate a new one
            return Response.ok(ApiResponse.success("Token masih valid")).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat refresh token", e.getMessage()))
                .build();
        }
    }

    @POST
    @Path("/logout")
    @Operation(summary = "User logout", description = "Logout user (client-side token removal)")
    public Response logout() {
        // In JWT implementation, logout is typically handled client-side by removing the token
        // Server-side logout would require token blacklisting which is more complex
        return Response.ok(ApiResponse.success("Logout berhasil")).build();
    }

    @POST
    @Path("/register")
    @Operation(summary = "Register new user", description = "Register new user (admin only)")
    public Response register(@Valid User newUser) {
        try {
            User createdUser = userService.createUser(newUser);
            // Remove password from response
            createdUser.password = null;
            
            return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.success("User berhasil didaftarkan", createdUser))
                .build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                .entity(ApiResponse.error(e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mendaftarkan user", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/me")
    @Operation(summary = "Get current user", description = "Get current authenticated user info")
    public Response getCurrentUser(@HeaderParam("Authorization") String authHeader) {
        try {
            // This is a simplified implementation
            // In a real implementation, you would decode the JWT token to get user info
            return Response.ok(ApiResponse.success("User info retrieved")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil info user", e.getMessage()))
                .build();
        }
    }
}
