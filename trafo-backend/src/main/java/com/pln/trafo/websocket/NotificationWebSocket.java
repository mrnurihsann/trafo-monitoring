package com.pln.trafo.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pln.trafo.entity.Notification;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/notifications/{userId}")
@ApplicationScoped
public class NotificationWebSocket {

    private static final Logger LOG = Logger.getLogger(NotificationWebSocket.class);
    private static final Map<Long, Session> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        LOG.infof("WebSocket opened for user: %d", userId);
        sessions.put(userId, session);
        
        // Send welcome message
        try {
            session.getAsyncRemote().sendText("{\"type\":\"connected\",\"message\":\"Connected to notification service\"}");
        } catch (Exception e) {
            LOG.errorf(e, "Error sending welcome message to user %d", userId);
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") Long userId) {
        LOG.infof("WebSocket closed for user: %d", userId);
        sessions.remove(userId);
    }

    @OnError
    public void onError(Session session, @PathParam("userId") Long userId, Throwable throwable) {
        LOG.errorf(throwable, "WebSocket error for user: %d", userId);
        sessions.remove(userId);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("userId") Long userId) {
        LOG.infof("Received message from user %d: %s", userId, message);
        
        // Handle ping/pong for connection keep-alive
        if ("ping".equals(message)) {
            try {
                Session session = sessions.get(userId);
                if (session != null && session.isOpen()) {
                    session.getAsyncRemote().sendText("pong");
                }
            } catch (Exception e) {
                LOG.errorf(e, "Error sending pong to user %d", userId);
            }
        }
    }

    public void sendNotificationToUser(Long userId, Notification notification) {
        Session session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                NotificationMessage message = new NotificationMessage(
                    "notification",
                    notification.id,
                    notification.title,
                    notification.message,
                    notification.type.name(),
                    notification.createdAt.toString()
                );
                
                String jsonMessage = objectMapper.writeValueAsString(message);
                session.getAsyncRemote().sendText(jsonMessage);
                LOG.infof("Sent notification to user %d: %s", userId, notification.title);
            } catch (Exception e) {
                LOG.errorf(e, "Error sending notification to user %d", userId);
                // Remove invalid session
                sessions.remove(userId);
            }
        } else {
            LOG.debugf("No active session found for user %d", userId);
        }
    }

    public void broadcastToAllUsers(String type, Object data) {
        try {
            BroadcastMessage message = new BroadcastMessage(type, data);
            String jsonMessage = objectMapper.writeValueAsString(message);
            
            sessions.entrySet().removeIf(entry -> {
                Session session = entry.getValue();
                if (session.isOpen()) {
                    try {
                        session.getAsyncRemote().sendText(jsonMessage);
                        return false; // Keep this session
                    } catch (Exception e) {
                        LOG.errorf(e, "Error broadcasting to user %d", entry.getKey());
                        return true; // Remove this session
                    }
                } else {
                    return true; // Remove closed session
                }
            });
            
            LOG.infof("Broadcasted message to %d active sessions", sessions.size());
        } catch (Exception e) {
            LOG.errorf(e, "Error broadcasting message");
        }
    }

    public void sendSystemAlert(String title, String message, String level) {
        SystemAlert alert = new SystemAlert("system_alert", title, message, level);
        try {
            String jsonMessage = objectMapper.writeValueAsString(alert);
            
            sessions.entrySet().removeIf(entry -> {
                Session session = entry.getValue();
                if (session.isOpen()) {
                    try {
                        session.getAsyncRemote().sendText(jsonMessage);
                        return false;
                    } catch (Exception e) {
                        LOG.errorf(e, "Error sending system alert to user %d", entry.getKey());
                        return true;
                    }
                } else {
                    return true;
                }
            });
            
            LOG.infof("Sent system alert to %d active sessions: %s", sessions.size(), title);
        } catch (Exception e) {
            LOG.errorf(e, "Error sending system alert");
        }
    }

    public int getActiveSessionsCount() {
        // Clean up closed sessions
        sessions.entrySet().removeIf(entry -> !entry.getValue().isOpen());
        return sessions.size();
    }

    public boolean isUserConnected(Long userId) {
        Session session = sessions.get(userId);
        return session != null && session.isOpen();
    }

    // DTO classes for WebSocket messages
    public static class NotificationMessage {
        public String type;
        public Long id;
        public String title;
        public String message;
        public String notificationType;
        public String timestamp;

        public NotificationMessage() {}

        public NotificationMessage(String type, Long id, String title, String message, 
                                 String notificationType, String timestamp) {
            this.type = type;
            this.id = id;
            this.title = title;
            this.message = message;
            this.notificationType = notificationType;
            this.timestamp = timestamp;
        }
    }

    public static class BroadcastMessage {
        public String type;
        public Object data;

        public BroadcastMessage() {}

        public BroadcastMessage(String type, Object data) {
            this.type = type;
            this.data = data;
        }
    }

    public static class SystemAlert {
        public String type;
        public String title;
        public String message;
        public String level;

        public SystemAlert() {}

        public SystemAlert(String type, String title, String message, String level) {
            this.type = type;
            this.title = title;
            this.message = message;
            this.level = level;
        }
    }
}
