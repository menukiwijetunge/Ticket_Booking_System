package menuki.ticketing.model;

import java.time.LocalDateTime;


/**
 * This is the model class for order. This defines the basic structure and operations for an order.
 */
public class Order {
    private int id;
    private String userId;
    private String eventId;
    private int totalCents;
    private String status;
    private LocalDateTime createdAt;

    //Constructors
    public Order() {}

    public Order(int id, String userId, String eventId, int totalCents, String status, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.eventId = eventId;
        this.totalCents = totalCents;
        this.status = status;
        this.createdAt = createdAt;
    }

    //Getters
    public int getId() { return id; }
    public String getUserId() { return userId; }
    public String getEventId() { return eventId; }
    public int getTotalCents() { return totalCents; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    //Setters
    public void setId(int id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public void setTotalCents(int totalCents) { this.totalCents = totalCents; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
