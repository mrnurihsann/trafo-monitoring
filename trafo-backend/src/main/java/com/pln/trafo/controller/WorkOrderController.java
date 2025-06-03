package com.pln.trafo.controller;

import com.pln.trafo.dto.ApiResponse;
import com.pln.trafo.entity.WorkOrder;
import com.pln.trafo.entity.User;
import com.pln.trafo.entity.Transformer;
import com.pln.trafo.service.UserService;
import com.pln.trafo.service.TransformerService;
import com.pln.trafo.service.NotificationService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/api/work-orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Work Orders", description = "Work order management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class WorkOrderController {

    @Inject
    UserService userService;

    @Inject
    TransformerService transformerService;

    @Inject
    NotificationService notificationService;

    @GET
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get all work orders", description = "Retrieve all work orders")
    public Response getAllWorkOrders() {
        try {
            List<WorkOrder> workOrders = WorkOrder.listAll();
            return Response.ok(ApiResponse.success(workOrders)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil data work order", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get work order by ID", description = "Retrieve work order by ID")
    public Response getWorkOrderById(@PathParam("id") Long id) {
        try {
            WorkOrder workOrder = WorkOrder.findById(id);
            if (workOrder == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Work order tidak ditemukan"))
                    .build();
            }
            return Response.ok(ApiResponse.success(workOrder)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil data work order", e.getMessage()))
                .build();
        }
    }

    @POST
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR"})
    @Transactional
    @Operation(summary = "Create work order", description = "Create new work order")
    public Response createWorkOrder(@Valid WorkOrderCreateRequest request) {
        try {
            Transformer transformer = transformerService.getTransformerById(request.transformerId);
            
            WorkOrder workOrder = new WorkOrder();
            workOrder.transformer = transformer;
            workOrder.title = request.title;
            workOrder.description = request.description;
            workOrder.status = WorkOrder.WorkOrderStatus.PENDING;
            
            if (request.assignedToId != null) {
                User assignedTo = userService.getUserById(request.assignedToId);
                workOrder.assignedTo = assignedTo;
                
                // Send notification to assigned user
                notificationService.notifyWorkOrderAssignment(
                    assignedTo, request.title, transformer.kodeTrafo
                );
            }
            
            workOrder.persist();
            
            return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.success("Work order berhasil dibuat", workOrder))
                .build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                .entity(ApiResponse.error(e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat membuat work order", e.getMessage()))
                .build();
        }
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR"})
    @Transactional
    @Operation(summary = "Update work order", description = "Update work order by ID")
    public Response updateWorkOrder(@PathParam("id") Long id, @Valid WorkOrderUpdateRequest request) {
        try {
            WorkOrder workOrder = WorkOrder.findById(id);
            if (workOrder == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Work order tidak ditemukan"))
                    .build();
            }

            workOrder.title = request.title;
            workOrder.description = request.description;
            workOrder.status = request.status;
            
            if (request.assignedToId != null) {
                User assignedTo = userService.getUserById(request.assignedToId);
                workOrder.assignedTo = assignedTo;
            }

            // If status is completed, set completion time
            if (request.status == WorkOrder.WorkOrderStatus.COMPLETED) {
                workOrder.complete();
                
                // Send completion notification
                notificationService.notifyWorkOrderCompletion(
                    workOrder.title, workOrder.transformer.kodeTrafo
                );
            }

            return Response.ok(ApiResponse.success("Work order berhasil diperbarui", workOrder)).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                .entity(ApiResponse.error(e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat memperbarui work order", e.getMessage()))
                .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "ENGINEER"})
    @Transactional
    @Operation(summary = "Delete work order", description = "Delete work order by ID")
    public Response deleteWorkOrder(@PathParam("id") Long id) {
        try {
            WorkOrder workOrder = WorkOrder.findById(id);
            if (workOrder == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Work order tidak ditemukan"))
                    .build();
            }
            
            workOrder.delete();
            return Response.ok(ApiResponse.success("Work order berhasil dihapus")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat menghapus work order", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/transformer/{transformerId}")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get work orders by transformer", description = "Retrieve work orders for specific transformer")
    public Response getWorkOrdersByTransformer(@PathParam("transformerId") Long transformerId) {
        try {
            List<WorkOrder> workOrders = WorkOrder.findByTransformerId(transformerId);
            return Response.ok(ApiResponse.success(workOrders)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil data work order", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/assigned/{userId}")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR"})
    @Operation(summary = "Get work orders assigned to user", description = "Retrieve work orders assigned to specific user")
    public Response getWorkOrdersByAssignedUser(@PathParam("userId") Long userId) {
        try {
            User user = userService.getUserById(userId);
            List<WorkOrder> workOrders = WorkOrder.findByAssignedTo(user);
            return Response.ok(ApiResponse.success(workOrders)).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                .entity(ApiResponse.error(e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil data work order", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/status/{status}")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get work orders by status", description = "Retrieve work orders by status")
    public Response getWorkOrdersByStatus(@PathParam("status") String status) {
        try {
            WorkOrder.WorkOrderStatus workOrderStatus = WorkOrder.WorkOrderStatus.valueOf(status.toUpperCase());
            List<WorkOrder> workOrders = WorkOrder.findByStatus(workOrderStatus);
            return Response.ok(ApiResponse.success(workOrders)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(ApiResponse.error("Status tidak valid"))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil data work order", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/pending")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get pending work orders", description = "Retrieve all pending work orders")
    public Response getPendingWorkOrders() {
        try {
            List<WorkOrder> workOrders = WorkOrder.findPendingWorkOrders();
            return Response.ok(ApiResponse.success(workOrders)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil data work order", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/active")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get active work orders", description = "Retrieve all active (pending + in progress) work orders")
    public Response getActiveWorkOrders() {
        try {
            List<WorkOrder> workOrders = WorkOrder.findActiveWorkOrders();
            return Response.ok(ApiResponse.success(workOrders)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil data work order", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/overdue")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR"})
    @Operation(summary = "Get overdue work orders", description = "Retrieve overdue work orders (more than 7 days old)")
    public Response getOverdueWorkOrders() {
        try {
            List<WorkOrder> workOrders = WorkOrder.findOverdueWorkOrders();
            return Response.ok(ApiResponse.success(workOrders)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil data work order", e.getMessage()))
                .build();
        }
    }

    // DTO classes
    public static class WorkOrderCreateRequest {
        public Long transformerId;
        public String title;
        public String description;
        public Long assignedToId;
    }

    public static class WorkOrderUpdateRequest {
        public String title;
        public String description;
        public WorkOrder.WorkOrderStatus status;
        public Long assignedToId;
    }
}
