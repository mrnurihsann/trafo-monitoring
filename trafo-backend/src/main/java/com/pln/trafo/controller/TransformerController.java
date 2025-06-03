package com.pln.trafo.controller;

import com.pln.trafo.dto.ApiResponse;
import com.pln.trafo.entity.Transformer;
import com.pln.trafo.entity.StatusHistory;
import com.pln.trafo.entity.User;
import com.pln.trafo.service.TransformerService;
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

@Path("/api/transformers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Transformers", description = "Transformer management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class TransformerController {

    @Inject
    TransformerService transformerService;

    @Inject
    UserService userService;

    @GET
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get all transformers", description = "Retrieve all transformers")
    public Response getAllTransformers() {
        try {
            List<Transformer> transformers = transformerService.getAllTransformers();
            return Response.ok(ApiResponse.success(transformers)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil data transformer", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get transformer by ID", description = "Retrieve transformer by ID")
    public Response getTransformerById(@PathParam("id") Long id) {
        try {
            Transformer transformer = transformerService.getTransformerById(id);
            return Response.ok(ApiResponse.success(transformer)).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                .entity(ApiResponse.error(e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil data transformer", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/code/{kodeTrafo}")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get transformer by code", description = "Retrieve transformer by kode trafo")
    public Response getTransformerByKodeTrafo(@PathParam("kodeTrafo") String kodeTrafo) {
        try {
            Transformer transformer = transformerService.getTransformerByKodeTrafo(kodeTrafo);
            return Response.ok(ApiResponse.success(transformer)).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                .entity(ApiResponse.error(e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil data transformer", e.getMessage()))
                .build();
        }
    }

    @POST
    @RolesAllowed({"ADMIN", "ENGINEER"})
    @Operation(summary = "Create transformer", description = "Create new transformer")
    public Response createTransformer(@Valid Transformer transformer) {
        try {
            Transformer createdTransformer = transformerService.createTransformer(transformer);
            return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.success("Transformer berhasil dibuat", createdTransformer))
                .build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                .entity(ApiResponse.error(e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat membuat transformer", e.getMessage()))
                .build();
        }
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "ENGINEER"})
    @Operation(summary = "Update transformer", description = "Update transformer by ID")
    public Response updateTransformer(@PathParam("id") Long id, @Valid Transformer transformer) {
        try {
            Transformer updatedTransformer = transformerService.updateTransformer(id, transformer);
            return Response.ok(ApiResponse.success("Transformer berhasil diperbarui", updatedTransformer)).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                .entity(ApiResponse.error(e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat memperbarui transformer", e.getMessage()))
                .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Delete transformer", description = "Delete transformer by ID (Admin only)")
    public Response deleteTransformer(@PathParam("id") Long id) {
        try {
            transformerService.deleteTransformer(id);
            return Response.ok(ApiResponse.success("Transformer berhasil dihapus")).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                .entity(ApiResponse.error(e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat menghapus transformer", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/status/{status}")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get transformers by status", description = "Retrieve transformers by status")
    public Response getTransformersByStatus(@PathParam("status") String status) {
        try {
            Transformer.TransformerStatus transformerStatus = Transformer.TransformerStatus.valueOf(status.toUpperCase());
            List<Transformer> transformers = transformerService.getTransformersByStatus(transformerStatus);
            return Response.ok(ApiResponse.success(transformers)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(ApiResponse.error("Status tidak valid"))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil data transformer", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/priority/{priority}")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get transformers by priority", description = "Retrieve transformers by priority")
    public Response getTransformersByPriority(@PathParam("priority") String priority) {
        try {
            Transformer.Priority transformerPriority = Transformer.Priority.valueOf(priority.toUpperCase());
            List<Transformer> transformers = transformerService.getTransformersByPriority(transformerPriority);
            return Response.ok(ApiResponse.success(transformers)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(ApiResponse.error("Priority tidak valid"))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil data transformer", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/critical")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get critical transformers", description = "Retrieve critical transformers (fault status or critical priority)")
    public Response getCriticalTransformers() {
        try {
            List<Transformer> transformers = transformerService.getCriticalTransformers();
            return Response.ok(ApiResponse.success(transformers)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil data transformer kritikal", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/engineer/{engineerId}")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR"})
    @Operation(summary = "Get transformers by engineer", description = "Retrieve transformers assigned to specific engineer")
    public Response getTransformersByEngineer(@PathParam("engineerId") Long engineerId) {
        try {
            User engineer = userService.getUserById(engineerId);
            List<Transformer> transformers = transformerService.getTransformersByPicEngineer(engineer);
            return Response.ok(ApiResponse.success(transformers)).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                .entity(ApiResponse.error(e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil data transformer", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/search")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR", "VIEWER"})
    @Operation(summary = "Search transformers by location", description = "Search transformers by location")
    public Response searchTransformersByLocation(@QueryParam("lokasi") String lokasi) {
        try {
            if (lokasi == null || lokasi.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Parameter lokasi tidak boleh kosong"))
                    .build();
            }
            
            List<Transformer> transformers = transformerService.searchTransformersByLokasi(lokasi);
            return Response.ok(ApiResponse.success(transformers)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mencari transformer", e.getMessage()))
                .build();
        }
    }

    @PUT
    @Path("/{id}/status")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR"})
    @Operation(summary = "Update transformer status", description = "Update transformer status with history tracking")
    public Response updateTransformerStatus(@PathParam("id") Long id, StatusUpdateRequest request) {
        try {
            // In a real implementation, you would get the current user from JWT token
            User changedBy = userService.getUserById(1L); // Placeholder - should be current user
            
            Transformer updatedTransformer = transformerService.updateTransformerStatus(
                id, request.newStatus, changedBy, request.notes
            );
            
            return Response.ok(ApiResponse.success("Status transformer berhasil diperbarui", updatedTransformer)).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                .entity(ApiResponse.error(e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat memperbarui status transformer", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/{id}/status-history")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get transformer status history", description = "Retrieve status history for specific transformer")
    public Response getTransformerStatusHistory(@PathParam("id") Long transformerId) {
        try {
            List<StatusHistory> statusHistory = StatusHistory.findByTransformerId(transformerId);
            return Response.ok(ApiResponse.success(statusHistory)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil riwayat status", e.getMessage()))
                .build();
        }
    }

    // DTO for status update request
    public static class StatusUpdateRequest {
        public Transformer.TransformerStatus newStatus;
        public String notes;
    }
}
