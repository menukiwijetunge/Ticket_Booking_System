
package menuki.ticketing.model;

/**
 * This is the model class for OrderItem. This defines the basic structure and operations for an order item.
 */
public class OrderItem {
    private final int id;
    private final int orderId;
    private final String eventId;
    private final String rowLabel;
    private final int seatNumber;
    private final int priceCents;

    //Constructors
    public OrderItem(int id, int orderId, String eventId, String rowLabel, int seatNumber, int priceCents) {
        this.id = id;
        this.orderId = orderId;
        this.eventId = eventId;
        this.rowLabel = rowLabel;
        this.seatNumber = seatNumber;
        this.priceCents = priceCents;
    }


    //Getters
    public int getId() { return id; }
    public int getOrderId() { return orderId; }
    public String getEventId() { return eventId; }
    public String getRowLabel() { return rowLabel; }
    public int getSeatNumber() { return seatNumber; }
    public int getPriceCents() { return priceCents; }
}
