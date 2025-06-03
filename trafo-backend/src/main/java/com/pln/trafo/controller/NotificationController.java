package com.pln.trafo.controller;

import com.pln.trafo.dto.ApiResponse;
import com.pln.trafo.entity.Notification;
import com.pln.trafo.entity.User;
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

@Path("/api/notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Notifications", description = "Notification management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    @Inject
    NotificationService notificationService;

    @GET
    @Path("/user/{userId}")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get notifications for user", description = "Retrieve all notifications for specific user")
    public Response getNotificationsForUser(@PathParam("userId") Long userId) {
        try {
            List<Notification> notifications = notificationService.getNotificationsForUser(userId);
            return Response.ok(ApiResponse.success(notifications)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil notifikasi", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/user/{userId}/unread")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get unread notifications for user", description = "Retrieve unread notifications for specific user")
    public Response getUnreadNotificationsForUser(@PathParam("userId") Long userId) {
        try {
            List<Notification> notifications = notificationService.getUnreadNotificationsForUser(userId);
            return Response.ok(ApiResponse.success(notifications)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil notifikasi", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/user/{userId}/unread/count")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get unread notifications count", description = "Get count of unread notifications for specific user")
    public Response getUnreadNotificationsCount(@PathParam("userId") Long userId) {
        try {
            long count = notificationService.getUnreadCountForUser(userId);
            return Response.ok(ApiResponse.success("Jumlah notifikasi belum dibaca", count)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil jumlah notifikasi", e.getMessage()))
                .build();
        }
    }

    @PUT
    @Path("/{id}/mark-read")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR", "VIEWER"})
    @Operation(summary = "Mark notification as read", description = "Mark specific notification as read")
    public Response markNotificationAsRead(@PathParam("id") Long notificationId) {
        try {
            Notification notification = notificationService.markAsRead(notificationId);
            return Response.ok(ApiResponse.success("Notifikasi berhasil ditandai sebagai dibaca", notification)).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                .entity(ApiResponse.error(e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat menandai notifikasi", e.getMessage()))
                .build();
        }
    }

    @PUT
    @Path("/user/{userId}/mark-all-read")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR", "VIEWER"})
    @Operation(summary = "Mark all notifications as read", description = "Mark all notifications as read for specific user")
    public Response markAllNotificationsAsRead(@PathParam("userId") Long userId) {
        try {
            notificationService.markAllAsReadForUser(userId);
            return Response.ok(ApiResponse.success("Semua notifikasi berhasil ditandai sebagai dibaca")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat menandai semua notifikasi", e.getMessage()))
                .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR", "VIEWER"})
    @Operation(summary = "Delete notification", description = "Delete specific notification")
    public Response deleteNotification(@PathParam("id") Long notificationId) {
        try {
            notificationService.deleteNotification(notificationId);
            return Response.ok(ApiResponse.success("Notifikasi berhasil dihapus")).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                .entity(ApiResponse.error(e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat menghapus notifikasi", e.getMessage()))
                .build();
        }
    }

    @POST
    @RolesAllowed({"ADMIN", "ENGINEER"})
    @Operation(summary = "Create notification", description = "Create new notification (Admin and Engineer only)")
    public Response createNotification(@Valid NotificationCreateRequest request) {
        try {
            Notification notification = new Notification();
            notification.title = request.title;
            notification.message = request.message;
            notification.type = request.type;
            
            if (request.userId != null) {
                User user = User.findById(request.userId);
                if (user == null) {
                    return Response.status(Response.Status.NOT_FOUND)
                        .entity(ApiResponse.error("User tidak ditemukan"))
                        .build();
                }
                notification.user = user;
            }
            
            Notification createdNotification = notificationService.createNotification(notification);
            
            return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.success("Notifikasi berhasil dibuat", createdNotification))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat membuat notifikasi", e.getMessage()))
                .build();
        }
    }

    @POST
    @Path("/broadcast")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Broadcast notification", description = "Send notification to all users (Admin only)")
    public Response broadcastNotification(@Valid BroadcastNotificationRequest request) {
        try {
            notificationService.createBroadcastNotification(request.title, request.message, request.type);
            return Response.ok(ApiResponse.success("Notifikasi berhasil dikirim ke semua user")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengirim broadcast notifikasi", e.getMessage()))
                .build();
        }
    }

    @POST
    @Path("/role/{role}")
    @RolesAllowed({"ADMIN", "ENGINEER"})
    @Operation(summary = "Send notification to role", description = "Send notification to all users with specific role")
    public Response sendNotificationToRole(@PathParam("role") String role, @Valid RoleNotificationRequest request) {
        try {
            User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
            notificationService.createNotificationForRole(request.title, request.message, request.type, userRole);
            
            return Response.ok(ApiResponse.success("Notifikasi berhasil dikirim ke role " + role)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(ApiResponse.error("Role tidak valid"))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengirim notifikasi", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/recent")
    @RolesAllowed({"ADMIN", "ENGINEER"})
    @Operation(summary = "Get recent notifications", description = "Get recent notifications (Admin and Engineer only)")
    public Response getRecentNotifications(@QueryParam("limit") @DefaultValue("20") int limit) {
        try {
            List<Notification> notifications = notificationService.getRecentNotifications(limit);
            return Response.ok(ApiResponse.success(notifications)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil notifikasi terbaru", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/type/{type}")
    @RolesAllowed({"ADMIN", "ENGINEER"})
    @Operation(summary = "Get notifications by type", description = "Get notifications by type (Admin and Engineer only)")
    public Response getNotificationsByType(@PathParam("type") String type) {
        try {
            Notification.NotificationType notificationType = Notification.NotificationType.valueOf(type.toUpperCase());
            List<Notification> notifications = notificationService.getNotificationsByType(notificationType);
            return Response.ok(ApiResponse.success(notifications)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(ApiResponse.error("Tipe notifikasi tidak valid"))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil notifikasi", e.getMessage()))
                .build();
        }
    }

    // DTO classes
    public static class NotificationCreateRequest {
        public Long userId;
        public String title;
        public String message;
        public Notification.NotificationType type = Notification.NotificationType.INFO;
    }

    public static class BroadcastNotificationRequest {
        public String title;
        public String message;
        public Notification.NotificationType type = Notification.NotificationType.INFO;
    }

    public static class RoleNotificationRequest {
        public String title;
        public String message;
        public Notification.NotificationType type = Notification.NotificationType.INFO;
    }
}
