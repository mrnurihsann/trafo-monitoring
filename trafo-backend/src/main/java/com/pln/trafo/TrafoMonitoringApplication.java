package com.pln.trafo;

import io.quarkus.runtime.StartupEvent;
import org.jboss.logging.Logger;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TrafoMonitoringApplication {

    private static final Logger LOG = Logger.getLogger(TrafoMonitoringApplication.class);

    void onStart(@Observes StartupEvent ev) {
        LOG.info("🔌 PLN Transformer Monitoring Backend is starting...");
        LOG.info("📱 Frontend CORS enabled for development");
        LOG.info("🔐 JWT authentication configured");
        LOG.info("🔌 WebSocket notifications enabled");
        LOG.info("📊 Database migrations will run automatically");
        LOG.info("🚀 PLN Transformer Monitoring Backend started successfully!");
        LOG.info("📚 API Documentation available at: /q/swagger-ui");
        LOG.info("🏥 Health check available at: /q/health");
        LOG.info("📈 Metrics available at: /q/metrics");
    }
}
