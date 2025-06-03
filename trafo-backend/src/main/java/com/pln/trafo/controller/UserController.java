package com.pln.trafo.controller;

import com.pln.trafo.dto.ApiResponse;
import com.pln.trafo.entity.User;
import com.pln.trafo.service.UserService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Users", description = "User management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    @Inject
    UserService userService;

    @GET
    @RolesAllowed({"ADMIN", "ENGINEER"})
    @Operation(summary = "Get all users", description = "Retrieve all users (Admin and Engineer only)")
    public Response getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            // Remove passwords from response
            users.forEach(user -> user.password = null);
            
            return Response.ok(ApiResponse.success(users)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil data user", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "ENGINEER"})
    @Operation(summary = "Get user by ID", description = "Retrieve user by ID")
    public Response getUserById(@PathParam("id") Long id) {
        try {
            User user = userService.getUserById(id);
            user.password = null; // Remove password from response
            
            return Response.ok(ApiResponse.success(user)).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                .entity(ApiResponse.error(e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil data user", e.getMessage()))
                .build();
        }
    }

    @POST
    @RolesAllowed("ADMIN")
    @Operation(summary = "Create user", description = "Create new user (Admin only)")
    public Response createUser(@Valid User user) {
        try {
            User createdUser = userService.createUser(user);
            createdUser.password = null; // Remove password from response
            
            return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.success("User berhasil dibuat", createdUser))
                .build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                .entity(ApiResponse.error(e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat membuat user", e.getMessage()))
                .build();
        }
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Update user", description = "Update user by ID (Admin only)")
    public Response updateUser(@PathParam("id") Long id, @Valid User user) {
        try {
            User updatedUser = userService.updateUser(id, user);
            updatedUser.password = null; // Remove password from response
            
            return Response.ok(ApiResponse.success("User berhasil diperbarui", updatedUser)).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                .entity(ApiResponse.error(e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat memperbarui user", e.getMessage()))
                .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Delete user", description = "Delete user by ID (Admin only)")
    public Response deleteUser(@PathParam("id") Long id) {
        try {
            userService.deleteUser(id);
            return Response.ok(ApiResponse.success("User berhasil dihapus")).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                .entity(ApiResponse.error(e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat menghapus user", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/engineers")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR"})
    @Operation(summary = "Get all engineers", description = "Retrieve all users with ENGINEER role")
    public Response getEngineers() {
        try {
            List<User> engineers = userService.getEngineers();
            engineers.forEach(user -> user.password = null);
            
            return Response.ok(ApiResponse.success(engineers)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil data engineer", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/role/{role}")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Get users by role", description = "Retrieve users by role (Admin only)")
    public Response getUsersByRole(@PathParam("role") String role) {
        try {
            User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
            List<User> users = userService.getUsersByRole(userRole);
            users.forEach(user -> user.password = null);
            
            return Response.ok(ApiResponse.success(users)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(ApiResponse.error("Role tidak valid"))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil data user", e.getMessage()))
                .build();
        }
    }

    @PUT
    @Path("/{id}/change-password")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR", "VIEWER"})
    @Operation(summary = "Change password", description = "Change user password")
    public Response changePassword(@PathParam("id") Long id, ChangePasswordRequest request) {
        try {
            userService.changePassword(id, request.oldPassword, request.newPassword);
            return Response.ok(ApiResponse.success("Password berhasil diubah")).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                .entity(ApiResponse.error(e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengubah password", e.getMessage()))
                .build();
        }
    }

    // DTO for change password request
    public static class ChangePasswordRequest {
        public String oldPassword;
        public String newPassword;
    }
}
