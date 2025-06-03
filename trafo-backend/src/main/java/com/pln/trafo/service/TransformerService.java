package com.pln.trafo.service;

import com.pln.trafo.entity.Transformer;
import com.pln.trafo.entity.StatusHistory;
import com.pln.trafo.entity.User;
import com.pln.trafo.entity.Notification;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;

@ApplicationScoped
public class TransformerService {

    @Inject
    NotificationService notificationService;

    @Transactional
    public Transformer createTransformer(Transformer transformer) {
        // Check if kode trafo already exists
        if (Transformer.findByKodeTrafo(transformer.kodeTrafo) != null) {
            throw new WebApplicationException("Kode trafo sudah digunakan", Response.Status.CONFLICT);
        }

        transformer.persist();
        
        // Create notification for admins about new transformer
        notificationService.createNotificationForRole(
            "Transformer Baru Ditambahkan",
            "Transformer baru dengan kode " + transformer.kodeTrafo + " telah ditambahkan ke sistem",
            Notification.NotificationType.INFO,
            User.UserRole.ADMIN
        );

        return transformer;
    }

    @Transactional
    public Transformer updateTransformer(Long id, Transformer updatedTransformer) {
        Transformer existingTransformer = Transformer.findById(id);
        if (existingTransformer == null) {
            throw new WebApplicationException("Transformer tidak ditemukan", Response.Status.NOT_FOUND);
        }

        // Check if kode trafo is being changed and already exists
        if (!existingTransformer.kodeTrafo.equals(updatedTransformer.kodeTrafo)) {
            Transformer transformerWithSameCode = Transformer.findByKodeTrafo(updatedTransformer.kodeTrafo);
            if (transformerWithSameCode != null && !transformerWithSameCode.id.equals(id)) {
                throw new WebApplicationException("Kode trafo sudah digunakan", Response.Status.CONFLICT);
            }
        }

        // Track status change
        Transformer.TransformerStatus oldStatus = existingTransformer.status;
        Transformer.TransformerStatus newStatus = updatedTransformer.status;

        // Update fields
        existingTransformer.namaTrafo = updatedTransformer.namaTrafo;
        existingTransformer.kodeTrafo = updatedTransformer.kodeTrafo;
        existingTransformer.lokasi = updatedTransformer.lokasi;
        existingTransformer.kapasitasKva = updatedTransformer.kapasitasKva;
        existingTransformer.teganganKv = updatedTransformer.teganganKv;
        existingTransformer.status = updatedTransformer.status;
        existingTransformer.priority = updatedTransformer.priority;
        existingTransformer.progress = updatedTransformer.progress;
        existingTransformer.picEngineer = updatedTransformer.picEngineer;
        existingTransformer.tanggalMulai = updatedTransformer.tanggalMulai;
        existingTransformer.tanggalTarget = updatedTransformer.tanggalTarget;
        existingTransformer.notes = updatedTransformer.notes;

        return existingTransformer;
    }

    @Transactional
    public Transformer updateTransformerStatus(Long transformerId, Transformer.TransformerStatus newStatus, 
                                              User changedBy, String notes) {
        Transformer transformer = Transformer.findById(transformerId);
        if (transformer == null) {
            throw new WebApplicationException("Transformer tidak ditemukan", Response.Status.NOT_FOUND);
        }

        Transformer.TransformerStatus oldStatus = transformer.status;
        transformer.status = newStatus;

        // Create status history record
        StatusHistory statusHistory = new StatusHistory();
        statusHistory.transformer = transformer;
        statusHistory.oldStatus = oldStatus;
        statusHistory.newStatus = newStatus;
        statusHistory.changedBy = changedBy;
        statusHistory.notes = notes;
        statusHistory.persist();

        // Create notifications based on status change
        createStatusChangeNotifications(transformer, oldStatus, newStatus, changedBy);

        return transformer;
    }

    @Transactional
    public void deleteTransformer(Long id) {
        Transformer transformer = Transformer.findById(id);
        if (transformer == null) {
            throw new WebApplicationException("Transformer tidak ditemukan", Response.Status.NOT_FOUND);
        }
        transformer.delete();
    }

    public Transformer getTransformerById(Long id) {
        Transformer transformer = Transformer.findById(id);
        if (transformer == null) {
            throw new WebApplicationException("Transformer tidak ditemukan", Response.Status.NOT_FOUND);
        }
        return transformer;
    }

    public List<Transformer> getAllTransformers() {
        return Transformer.listAll();
    }

    public List<Transformer> getTransformersByStatus(Transformer.TransformerStatus status) {
        return Transformer.findByStatus(status);
    }

    public List<Transformer> getTransformersByPicEngineer(User engineer) {
        return Transformer.findByPicEngineer(engineer);
    }

    public List<Transformer> getTransformersByPriority(Transformer.Priority priority) {
        return Transformer.findByPriority(priority);
    }

    public List<Transformer> getCriticalTransformers() {
        return Transformer.findCriticalTransformers();
    }

    public List<Transformer> searchTransformersByLokasi(String lokasi) {
        return Transformer.findByLokasi(lokasi);
    }

    public Transformer getTransformerByKodeTrafo(String kodeTrafo) {
        Transformer transformer = Transformer.findByKodeTrafo(kodeTrafo);
        if (transformer == null) {
            throw new WebApplicationException("Transformer tidak ditemukan", Response.Status.NOT_FOUND);
        }
        return transformer;
    }

    private void createStatusChangeNotifications(Transformer transformer, 
                                               Transformer.TransformerStatus oldStatus,
                                               Transformer.TransformerStatus newStatus, 
                                               User changedBy) {
        String title = "Status Transformer Berubah";
        String message = String.format("Status transformer %s (%s) berubah dari %s menjadi %s", 
                                      transformer.namaTrafo, transformer.kodeTrafo, 
                                      oldStatus, newStatus);
        
        Notification.NotificationType notificationType = Notification.NotificationType.INFO;
        
        // Determine notification type based on new status
        switch (newStatus) {
            case FAULT:
                notificationType = Notification.NotificationType.ERROR;
                title = "FAULT: Transformer Bermasalah";
                break;
            case MAINTENANCE:
                notificationType = Notification.NotificationType.WARNING;
                title = "Transformer Dalam Maintenance";
                break;
            case OPERATIONAL:
                notificationType = Notification.NotificationType.SUCCESS;
                title = "Transformer Kembali Operational";
                break;
            case OFFLINE:
                notificationType = Notification.NotificationType.WARNING;
                title = "Transformer Offline";
                break;
        }

        // Notify admins and engineers
        notificationService.createNotificationForRole(title, message, notificationType, User.UserRole.ADMIN);
        notificationService.createNotificationForRole(title, message, notificationType, User.UserRole.ENGINEER);

        // Notify PIC Engineer if assigned
        if (transformer.picEngineer != null) {
            notificationService.createNotificationForUser(
                transformer.picEngineer, title, message, notificationType
            );
        }
    }
}
