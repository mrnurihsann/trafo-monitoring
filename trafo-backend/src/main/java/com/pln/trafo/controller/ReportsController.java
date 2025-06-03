package com.pln.trafo.controller;

import com.pln.trafo.dto.ApiResponse;
import com.pln.trafo.entity.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/api/reports")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Reports", description = "Reporting endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ReportsController {

    @Inject
    EntityManager em;

    @GET
    @Path("/transformers")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get transformer report", description = "Generate transformer report with filters")
    public Response getTransformerReport(
            @QueryParam("status") String status,
            @QueryParam("priority") String priority,
            @QueryParam("engineerId") Long engineerId,
            @QueryParam("lokasi") String lokasi,
            @QueryParam("startDate") String startDateStr,
            @QueryParam("endDate") String endDateStr) {
        
        try {
            StringBuilder queryBuilder = new StringBuilder("SELECT t FROM Transformer t WHERE 1=1");
            Map<String, Object> parameters = new HashMap<>();

            if (status != null && !status.isEmpty()) {
                queryBuilder.append(" AND t.status = :status");
                parameters.put("status", Transformer.TransformerStatus.valueOf(status.toUpperCase()));
            }

            if (priority != null && !priority.isEmpty()) {
                queryBuilder.append(" AND t.priority = :priority");
                parameters.put("priority", Transformer.Priority.valueOf(priority.toUpperCase()));
            }

            if (engineerId != null) {
                queryBuilder.append(" AND t.picEngineer.id = :engineerId");
                parameters.put("engineerId", engineerId);
            }

            if (lokasi != null && !lokasi.isEmpty()) {
                queryBuilder.append(" AND t.lokasi LIKE :lokasi");
                parameters.put("lokasi", "%" + lokasi + "%");
            }

            if (startDateStr != null && !startDateStr.isEmpty()) {
                LocalDateTime startDate = LocalDate.parse(startDateStr).atStartOfDay();
                queryBuilder.append(" AND t.createdAt >= :startDate");
                parameters.put("startDate", startDate);
            }

            if (endDateStr != null && !endDateStr.isEmpty()) {
                LocalDateTime endDate = LocalDate.parse(endDateStr).plusDays(1).atStartOfDay();
                queryBuilder.append(" AND t.createdAt < :endDate");
                parameters.put("endDate", endDate);
            }

            queryBuilder.append(" ORDER BY t.createdAt DESC");

            var query = em.createQuery(queryBuilder.toString(), Transformer.class);
            parameters.forEach(query::setParameter);

            List<Transformer> transformers = query.getResultList();

            Map<String, Object> report = new HashMap<>();
            report.put("transformers", transformers);
            report.put("totalCount", transformers.size());
            report.put("filters", Map.of(
                "status", status,
                "priority", priority,
                "engineerId", engineerId,
                "lokasi", lokasi,
                "startDate", startDateStr,
                "endDate", endDateStr
            ));

            // Add summary statistics
            Map<String, Long> statusSummary = new HashMap<>();
            Map<String, Long> prioritySummary = new HashMap<>();
            
            for (Transformer t : transformers) {
                statusSummary.merge(t.status.name(), 1L, Long::sum);
                prioritySummary.merge(t.priority.name(), 1L, Long::sum);
            }
            
            report.put("statusSummary", statusSummary);
            report.put("prioritySummary", prioritySummary);

            return Response.ok(ApiResponse.success(report)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat membuat laporan transformer", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/work-orders")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get work order report", description = "Generate work order report with filters")
    public Response getWorkOrderReport(
            @QueryParam("status") String status,
            @QueryParam("assignedTo") Long assignedToId,
            @QueryParam("transformerId") Long transformerId,
            @QueryParam("startDate") String startDateStr,
            @QueryParam("endDate") String endDateStr) {
        
        try {
            StringBuilder queryBuilder = new StringBuilder("SELECT w FROM WorkOrder w WHERE 1=1");
            Map<String, Object> parameters = new HashMap<>();

            if (status != null && !status.isEmpty()) {
                queryBuilder.append(" AND w.status = :status");
                parameters.put("status", WorkOrder.WorkOrderStatus.valueOf(status.toUpperCase()));
            }

            if (assignedToId != null) {
                queryBuilder.append(" AND w.assignedTo.id = :assignedTo");
                parameters.put("assignedTo", assignedToId);
            }

            if (transformerId != null) {
                queryBuilder.append(" AND w.transformer.id = :transformerId");
                parameters.put("transformerId", transformerId);
            }

            if (startDateStr != null && !startDateStr.isEmpty()) {
                LocalDateTime startDate = LocalDate.parse(startDateStr).atStartOfDay();
                queryBuilder.append(" AND w.createdAt >= :startDate");
                parameters.put("startDate", startDate);
            }

            if (endDateStr != null && !endDateStr.isEmpty()) {
                LocalDateTime endDate = LocalDate.parse(endDateStr).plusDays(1).atStartOfDay();
                queryBuilder.append(" AND w.createdAt < :endDate");
                parameters.put("endDate", endDate);
            }

            queryBuilder.append(" ORDER BY w.createdAt DESC");

            var query = em.createQuery(queryBuilder.toString(), WorkOrder.class);
            parameters.forEach(query::setParameter);

            List<WorkOrder> workOrders = query.getResultList();

            Map<String, Object> report = new HashMap<>();
            report.put("workOrders", workOrders);
            report.put("totalCount", workOrders.size());
            report.put("filters", Map.of(
                "status", status,
                "assignedTo", assignedToId,
                "transformerId", transformerId,
                "startDate", startDateStr,
                "endDate", endDateStr
            ));

            // Add summary statistics
            Map<String, Long> statusSummary = new HashMap<>();
            Map<String, Long> assigneeSummary = new HashMap<>();
            
            for (WorkOrder wo : workOrders) {
                statusSummary.merge(wo.status.name(), 1L, Long::sum);
                if (wo.assignedTo != null) {
                    assigneeSummary.merge(wo.assignedTo.username, 1L, Long::sum);
                } else {
                    assigneeSummary.merge("Unassigned", 1L, Long::sum);
                }
            }
            
            report.put("statusSummary", statusSummary);
            report.put("assigneeSummary", assigneeSummary);

            return Response.ok(ApiResponse.success(report)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat membuat laporan work order", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/status-history")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get status history report", description = "Generate status change history report")
    public Response getStatusHistoryReport(
            @QueryParam("transformerId") Long transformerId,
            @QueryParam("changedBy") Long changedById,
            @QueryParam("oldStatus") String oldStatus,
            @QueryParam("newStatus") String newStatus,
            @QueryParam("startDate") String startDateStr,
            @QueryParam("endDate") String endDateStr) {
        
        try {
            StringBuilder queryBuilder = new StringBuilder("SELECT sh FROM StatusHistory sh WHERE 1=1");
            Map<String, Object> parameters = new HashMap<>();

            if (transformerId != null) {
                queryBuilder.append(" AND sh.transformer.id = :transformerId");
                parameters.put("transformerId", transformerId);
            }

            if (changedById != null) {
                queryBuilder.append(" AND sh.changedBy.id = :changedBy");
                parameters.put("changedBy", changedById);
            }

            if (oldStatus != null && !oldStatus.isEmpty()) {
                queryBuilder.append(" AND sh.oldStatus = :oldStatus");
                parameters.put("oldStatus", Transformer.TransformerStatus.valueOf(oldStatus.toUpperCase()));
            }

            if (newStatus != null && !newStatus.isEmpty()) {
                queryBuilder.append(" AND sh.newStatus = :newStatus");
                parameters.put("newStatus", Transformer.TransformerStatus.valueOf(newStatus.toUpperCase()));
            }

            if (startDateStr != null && !startDateStr.isEmpty()) {
                LocalDateTime startDate = LocalDate.parse(startDateStr).atStartOfDay();
                queryBuilder.append(" AND sh.changedAt >= :startDate");
                parameters.put("startDate", startDate);
            }

            if (endDateStr != null && !endDateStr.isEmpty()) {
                LocalDateTime endDate = LocalDate.parse(endDateStr).plusDays(1).atStartOfDay();
                queryBuilder.append(" AND sh.changedAt < :endDate");
                parameters.put("endDate", endDate);
            }

            queryBuilder.append(" ORDER BY sh.changedAt DESC");

            var query = em.createQuery(queryBuilder.toString(), StatusHistory.class);
            parameters.forEach(query::setParameter);

            List<StatusHistory> statusHistories = query.getResultList();

            Map<String, Object> report = new HashMap<>();
            report.put("statusHistories", statusHistories);
            report.put("totalCount", statusHistories.size());
            report.put("filters", Map.of(
                "transformerId", transformerId,
                "changedBy", changedById,
                "oldStatus", oldStatus,
                "newStatus", newStatus,
                "startDate", startDateStr,
                "endDate", endDateStr
            ));

            // Add summary statistics
            Map<String, Long> statusChangeSummary = new HashMap<>();
            Map<String, Long> changedBySummary = new HashMap<>();
            
            for (StatusHistory sh : statusHistories) {
                String changeKey = (sh.oldStatus != null ? sh.oldStatus.name() : "NULL") + " -> " + sh.newStatus.name();
                statusChangeSummary.merge(changeKey, 1L, Long::sum);
                changedBySummary.merge(sh.changedBy.username, 1L, Long::sum);
            }
            
            report.put("statusChangeSummary", statusChangeSummary);
            report.put("changedBySummary", changedBySummary);

            return Response.ok(ApiResponse.success(report)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat membuat laporan riwayat status", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/performance")
    @RolesAllowed({"ADMIN", "ENGINEER"})
    @Operation(summary = "Get performance report", description = "Generate system performance report")
    public Response getPerformanceReport(
            @QueryParam("period") @DefaultValue("30") int periodDays) {
        
        try {
            LocalDateTime startDate = LocalDateTime.now().minusDays(periodDays);
            
            // Get transformer status distribution over time
            String transformerQuery = """
                SELECT DATE(t.createdAt) as date, t.status, COUNT(t) as count
                FROM Transformer t 
                WHERE t.createdAt >= :startDate
                GROUP BY DATE(t.createdAt), t.status
                ORDER BY date DESC
                """;
            
            var transformerResults = em.createQuery(transformerQuery)
                .setParameter("startDate", startDate)
                .getResultList();

            // Get work order completion rate
            String workOrderQuery = """
                SELECT DATE(w.createdAt) as date, w.status, COUNT(w) as count
                FROM WorkOrder w 
                WHERE w.createdAt >= :startDate
                GROUP BY DATE(w.createdAt), w.status
                ORDER BY date DESC
                """;
            
            var workOrderResults = em.createQuery(workOrderQuery)
                .setParameter("startDate", startDate)
                .getResultList();

            // Get status changes frequency
            String statusChangeQuery = """
                SELECT DATE(sh.changedAt) as date, sh.newStatus, COUNT(sh) as count
                FROM StatusHistory sh 
                WHERE sh.changedAt >= :startDate
                GROUP BY DATE(sh.changedAt), sh.newStatus
                ORDER BY date DESC
                """;
            
            var statusChangeResults = em.createQuery(statusChangeQuery)
                .setParameter("startDate", startDate)
                .getResultList();

            Map<String, Object> report = new HashMap<>();
            report.put("period", periodDays + " days");
            report.put("startDate", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
            report.put("endDate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            report.put("transformerTrends", transformerResults);
            report.put("workOrderTrends", workOrderResults);
            report.put("statusChangeTrends", statusChangeResults);

            // Calculate overall metrics
            long totalTransformers = Transformer.count();
            long operationalTransformers = Transformer.countByStatus(Transformer.TransformerStatus.OPERATIONAL);
            long faultTransformers = Transformer.countByStatus(Transformer.TransformerStatus.FAULT);
            
            double operationalRate = totalTransformers > 0 ? (double) operationalTransformers / totalTransformers * 100 : 0;
            double faultRate = totalTransformers > 0 ? (double) faultTransformers / totalTransformers * 100 : 0;

            Map<String, Object> metrics = new HashMap<>();
            metrics.put("totalTransformers", totalTransformers);
            metrics.put("operationalRate", operationalRate);
            metrics.put("faultRate", faultRate);
            metrics.put("systemHealthScore", operationalRate);
            
            report.put("overallMetrics", metrics);

            return Response.ok(ApiResponse.success(report)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat membuat laporan performa", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/summary")
    @RolesAllowed({"ADMIN", "ENGINEER", "OPERATOR", "VIEWER"})
    @Operation(summary = "Get summary report", description = "Generate comprehensive summary report")
    public Response getSummaryReport() {
        try {
            Map<String, Object> summary = new HashMap<>();
            
            // Transformer summary
            Map<String, Object> transformerSummary = new HashMap<>();
            transformerSummary.put("total", Transformer.count());
            transformerSummary.put("operational", Transformer.countByStatus(Transformer.TransformerStatus.OPERATIONAL));
            transformerSummary.put("maintenance", Transformer.countByStatus(Transformer.TransformerStatus.MAINTENANCE));
            transformerSummary.put("fault", Transformer.countByStatus(Transformer.TransformerStatus.FAULT));
            transformerSummary.put("offline", Transformer.countByStatus(Transformer.TransformerStatus.OFFLINE));
            
            // Work order summary
            Map<String, Object> workOrderSummary = new HashMap<>();
            workOrderSummary.put("total", WorkOrder.count());
            workOrderSummary.put("pending", WorkOrder.countByStatus(WorkOrder.WorkOrderStatus.PENDING));
            workOrderSummary.put("inProgress", WorkOrder.countByStatus(WorkOrder.WorkOrderStatus.IN_PROGRESS));
            workOrderSummary.put("completed", WorkOrder.countByStatus(WorkOrder.WorkOrderStatus.COMPLETED));
            workOrderSummary.put("cancelled", WorkOrder.countByStatus(WorkOrder.WorkOrderStatus.CANCELLED));
            
            // User summary
            Map<String, Object> userSummary = new HashMap<>();
            userSummary.put("total", User.count());
            userSummary.put("admins", User.count("role", User.UserRole.ADMIN));
            userSummary.put("engineers", User.count("role", User.UserRole.ENGINEER));
            userSummary.put("operators", User.count("role", User.UserRole.OPERATOR));
            userSummary.put("viewers", User.count("role", User.UserRole.VIEWER));
            
            // Recent activity summary
            Map<String, Object> activitySummary = new HashMap<>();
            activitySummary.put("recentStatusChanges", StatusHistory.count("changedAt >= ?1", LocalDateTime.now().minusDays(7)));
            activitySummary.put("recentWorkOrders", WorkOrder.count("createdAt >= ?1", LocalDateTime.now().minusDays(7)));
            activitySummary.put("totalNotifications", Notification.count());
            
            summary.put("transformers", transformerSummary);
            summary.put("workOrders", workOrderSummary);
            summary.put("users", userSummary);
            summary.put("activity", activitySummary);
            summary.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            return Response.ok(ApiResponse.success(summary)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat membuat laporan ringkasan", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/export/csv")
    @RolesAllowed({"ADMIN", "ENGINEER"})
    @Produces("text/csv")
    @Operation(summary = "Export report to CSV", description = "Export report data to CSV format")
    public Response exportToCsv(@QueryParam("type") @DefaultValue("transformers") String type) {
        try {
            StringBuilder csv = new StringBuilder();
            
            switch (type.toLowerCase()) {
                case "transformers":
                    csv.append("ID,Nama Trafo,Kode Trafo,Lokasi,Kapasitas KVA,Tegangan KV,Status,Priority,Progress,PIC Engineer,Created At\n");
                    List<Transformer> transformers = Transformer.listAll();
                    for (Transformer t : transformers) {
                        csv.append(String.format("%d,%s,%s,%s,%.2f,%.2f,%s,%s,%d,%s,%s\n",
                            t.id, t.namaTrafo, t.kodeTrafo, t.lokasi, 
                            t.kapasitasKva, t.teganganKv, t.status, t.priority, t.progress,
                            t.picEngineer != null ? t.picEngineer.username : "",
                            t.createdAt != null ? t.createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : ""));
                    }
                    break;
                    
                case "workorders":
                    csv.append("ID,Transformer,Title,Description,Status,Assigned To,Created At,Completed At\n");
                    List<WorkOrder> workOrders = WorkOrder.listAll();
                    for (WorkOrder wo : workOrders) {
                        csv.append(String.format("%d,%s,%s,%s,%s,%s,%s,%s\n",
                            wo.id, wo.transformer.kodeTrafo, wo.title, 
                            wo.description != null ? wo.description.replace(",", ";") : "",
                            wo.status,
                            wo.assignedTo != null ? wo.assignedTo.username : "",
                            wo.createdAt != null ? wo.createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "",
                            wo.completedAt != null ? wo.completedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : ""));
                    }
                    break;
                    
                default:
                    return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Tipe export tidak valid"))
                        .build();
            }
            
            return Response.ok(csv.toString())
                .header("Content-Disposition", "attachment; filename=\"" + type + "_report.csv\"")
                .build();
                
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Terjadi kesalahan saat export CSV", e.getMessage()))
                .build();
        }
    }
}
