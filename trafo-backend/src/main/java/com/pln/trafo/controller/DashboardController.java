package com.pln.trafo.controller;

import com.pln.trafo.dto.ApiResponse;
import com.pln.trafo.dto.DashboardStats;
import com.pln.trafo.service.DashboardService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Path("/api/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Dashboard", description = "Dashboard and statistics endpoints")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    @Inject
    DashboardService dashboardService;

    @GET
    @Path("/stats")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get dashboard statistics", description = "Retrieve comprehensive dashboard statistics")
    public Response getDashboardStats(@QueryParam("userId") Long userId) {
        try {
            DashboardStats stats = dashboardService.getDashboardStats(userId);
            return Response.ok(ApiResponse.success(stats)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil statistik dashboard", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/recent-activities")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get recent activities", description = "Retrieve recent activities for dashboard")
    public Response getRecentActivities() {
        try {
            Map<String, Object> activities = dashboardService.getRecentActivities();
            return Response.ok(ApiResponse.success(activities)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil aktivitas terbaru", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/personal/{userId}")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get personal dashboard", description = "Retrieve personalized dashboard data for specific user")
    public Response getPersonalDashboard(@PathParam("userId") Long userId) {
        try {
            Map<String, Object> personalDashboard = dashboardService.getPersonalDashboard(userId);
            return Response.ok(ApiResponse.success(personalDashboard)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil dashboard personal", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/system-health")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get system health", description = "Retrieve system health metrics")
    public Response getSystemHealth() {
        try {
            Map<String, Object> health = dashboardService.getSystemHealth();
            return Response.ok(ApiResponse.success(health)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil status kesehatan sistem", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/summary")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get dashboard summary", description = "Retrieve summarized dashboard information")
    public Response getDashboardSummary(@QueryParam("userId") Long userId) {
        try {
            // Combine multiple dashboard data sources
            DashboardStats stats = dashboardService.getDashboardStats(userId);
            Map<String, Object> health = dashboardService.getSystemHealth();
            Map<String, Object> activities = dashboardService.getRecentActivities();
            
            DashboardSummary summary = new DashboardSummary();
            summary.stats = stats;
            summary.systemHealth = health;
            summary.recentActivities = activities;
            
            if (userId != null) {
                summary.personalDashboard = dashboardService.getPersonalDashboard(userId);
            }
            
            return Response.ok(ApiResponse.success(summary)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil ringkasan dashboard", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/critical-alerts")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR"})
    @Operation(summary = "Get critical alerts", description = "Retrieve critical alerts that need immediate attention")
    public Response getCriticalAlerts() {
        try {
            Map<String, Object> activities = dashboardService.getRecentActivities();
            Object criticalAlerts = activities.get("criticalAlerts");
            
            return Response.ok(ApiResponse.success("Critical alerts retrieved", criticalAlerts)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil alert kritikal", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/quick-stats")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get quick statistics", description = "Retrieve quick overview statistics")
    public Response getQuickStats() {
        try {
            DashboardStats stats = dashboardService.getDashboardStats(null);
            
            QuickStats quickStats = new QuickStats();
            quickStats.totalTransformers = stats.totalTransformers;
            quickStats.operationalTransformers = stats.operationalTransformers;
            quickStats.faultTransformers = stats.faultTransformers;
            quickStats.criticalTransformers = stats.criticalTransformers;
            quickStats.pendingWorkOrders = stats.pendingWorkOrders;
            quickStats.inProgressWorkOrders = stats.inProgressWorkOrders;
            
            // Calculate percentages
            if (stats.totalTransformers > 0) {
                quickStats.operationalPercentage = (double) stats.operationalTransformers / stats.totalTransformers * 100;
                quickStats.faultPercentage = (double) stats.faultTransformers / stats.totalTransformers * 100;
            }
            
            return Response.ok(ApiResponse.success(quickStats)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat mengambil statistik cepat", e.getMessage()))
                .build();
        }
    }

    // DTO classes for response
    public static class DashboardSummary {
        public DashboardStats stats;
        public Map<String, Object> systemHealth;
        public Map<String, Object> recentActivities;
        public Map<String, Object> personalDashboard;
    }

    public static class QuickStats {
        public long totalTransformers;
        public long operationalTransformers;
        public long faultTransformers;
        public long criticalTransformers;
        public long pendingWorkOrders;
        public long inProgressWorkOrders;
        public double operationalPercentage;
        public double faultPercentage;
    }
}
