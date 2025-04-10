package com.realestate.services;

import com.realestate.models.*;
import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;

/**
 * Service class for handling system notifications and alerts.
 * Demonstrates Observer pattern and event handling.
 */
public class NotificationService {
    // Singleton instance
    private static NotificationService instance;
    
    // Thread pool for async notification processing
    private final ExecutorService notificationExecutor;
    
    // Observer pattern implementation
    private final Map<String, Set<NotificationObserver>> observers;
    
    // Notification queues
    private final BlockingQueue<Notification> notificationQueue;
    private final Map<String, List<Notification>> userNotifications;
    
    // Rate limiting
    private final Map<String, RateLimiter> rateLimiters;
    private static final int MAX_NOTIFICATIONS_PER_HOUR = 10;

    /**
     * Notification types enum
     */
    public enum NotificationType {
        PROPERTY_LISTED,
        PRICE_CHANGE,
        PROPERTY_SOLD,
        NEW_INQUIRY,
        VIEWING_SCHEDULED,
        OFFER_RECEIVED,
        TRANSACTION_UPDATE,
        SYSTEM_ALERT
    }

    /**
     * Notification class
     */
    public static class Notification {
        private final String id;
        private final NotificationType type;
        private final String userId;
        private final String title;
        private final String message;
        private final Map<String, String> metadata;
        private final LocalDateTime timestamp;
        private boolean read;

        public Notification(NotificationType type, String userId, 
                          String title, String message, 
                          Map<String, String> metadata) {
            this.id = UUID.randomUUID().toString();
            this.type = type;
            this.userId = userId;
            this.title = title;
            this.message = message;
            this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
            this.timestamp = LocalDateTime.now();
            this.read = false;
        }

        // Getters
        public String getId() { return id; }
        public NotificationType getType() { return type; }
        public String getUserId() { return userId; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public Map<String, String> getMetadata() { return new HashMap<>(metadata); }
        public LocalDateTime getTimestamp() { return timestamp; }
        public boolean isRead() { return read; }
        
        // Setters
        public void setRead(boolean read) { this.read = read; }
    }

    /**
     * Observer interface for notification handling
     */
    public interface NotificationObserver {
        void onNotification(Notification notification);
    }

    /**
     * Rate limiter for notifications
     */
    private static class RateLimiter {
        private final Queue<LocalDateTime> notifications;
        private final int maxPerHour;

        public RateLimiter(int maxPerHour) {
            this.notifications = new ConcurrentLinkedQueue<>();
            this.maxPerHour = maxPerHour;
        }

        public boolean tryAcquire() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneHourAgo = now.minusHours(1);
            
            // Remove old timestamps
            while (!notifications.isEmpty() && 
                   notifications.peek().isBefore(oneHourAgo)) {
                notifications.poll();
            }
            
            // Check if under limit
            if (notifications.size() < maxPerHour) {
                notifications.offer(now);
                return true;
            }
            return false;
        }
    }

    // Private constructor for singleton pattern
    private NotificationService() {
        this.notificationExecutor = Executors.newFixedThreadPool(3);
        this.observers = new ConcurrentHashMap<>();
        this.notificationQueue = new LinkedBlockingQueue<>();
        this.userNotifications = new ConcurrentHashMap<>();
        this.rateLimiters = new ConcurrentHashMap<>();
        
        // Start notification processor
        startNotificationProcessor();
    }

    /**
     * Gets the singleton instance
     */
    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    /**
     * Registers an observer for notifications
     */
    public void registerObserver(String userId, NotificationObserver observer) {
        observers.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
                .add(observer);
    }

    /**
     * Unregisters an observer
     */
    public void unregisterObserver(String userId, NotificationObserver observer) {
        Set<NotificationObserver> userObservers = observers.get(userId);
        if (userObservers != null) {
            userObservers.remove(observer);
        }
    }

    /**
     * Sends a notification
     */
    public void sendNotification(NotificationType type, String userId,
                               String title, String message,
                               Map<String, String> metadata) {
        // Check rate limit
        RateLimiter limiter = rateLimiters.computeIfAbsent(userId,
            k -> new RateLimiter(MAX_NOTIFICATIONS_PER_HOUR));
            
        if (!limiter.tryAcquire()) {
            // Log rate limit exceeded
            return;
        }

        Notification notification = new Notification(type, userId, title, message, metadata);
        notificationQueue.offer(notification);
    }

    /**
     * Gets notifications for a user
     */
    public List<Notification> getUserNotifications(String userId) {
        return userNotifications.getOrDefault(userId, new ArrayList<>());
    }

    /**
     * Gets unread notifications count for a user
     */
    public int getUnreadCount(String userId) {
        return (int) getUserNotifications(userId).stream()
            .filter(n -> !n.isRead())
            .count();
    }

    /**
     * Marks a notification as read
     */
    public void markAsRead(String userId, String notificationId) {
        List<Notification> notifications = userNotifications.get(userId);
        if (notifications != null) {
            notifications.stream()
                .filter(n -> n.getId().equals(notificationId))
                .findFirst()
                .ifPresent(n -> n.setRead(true));
        }
    }

    /**
     * Marks all notifications as read for a user
     */
    public void markAllAsRead(String userId) {
        List<Notification> notifications = userNotifications.get(userId);
        if (notifications != null) {
            notifications.forEach(n -> n.setRead(true));
        }
    }

    /**
     * Deletes a notification
     */
    public void deleteNotification(String userId, String notificationId) {
        List<Notification> notifications = userNotifications.get(userId);
        if (notifications != null) {
            notifications.removeIf(n -> n.getId().equals(notificationId));
        }
    }

    /**
     * Clears all notifications for a user
     */
    public void clearNotifications(String userId) {
        userNotifications.remove(userId);
    }

    // Private helper methods

    private void startNotificationProcessor() {
        Thread processor = new Thread(() -> {
            while (true) {
                try {
                    Notification notification = notificationQueue.take();
                    processNotification(notification);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        processor.setDaemon(true);
        processor.start();
    }

    private void processNotification(Notification notification) {
        notificationExecutor.submit(() -> {
            // Store notification
            userNotifications.computeIfAbsent(notification.getUserId(),
                k -> Collections.synchronizedList(new ArrayList<>()))
                .add(notification);

            // Notify observers
            Set<NotificationObserver> userObservers = observers.get(notification.getUserId());
            if (userObservers != null) {
                userObservers.forEach(observer -> 
                    observer.onNotification(notification)
                );
            }
        });
    }
}
