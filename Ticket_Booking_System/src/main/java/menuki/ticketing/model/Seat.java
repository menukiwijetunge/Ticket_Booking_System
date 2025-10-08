package menuki.ticketing.model;

import java.util.Objects;

/**
 * This is the model class for Seat. This defines the basic structure and operations for a seat.
 */
public class Seat {
    private final String eventId;
    private final String rowLabel;
    private final int seat_number;
    private final SeatType type;
    private SeatStatus status;
    private final int priceCents;

    //Constructor
    public Seat(String eventId, String rowLabel, int seat_number, SeatType type, SeatStatus status, int priceCents) {
        this.eventId = eventId;
        this.rowLabel = rowLabel;
        this.seat_number = seat_number;
        this.type = type;
        this.status = status;
        this.priceCents = priceCents;
    }
    //Getters
    public String getEventId() { return eventId; }
    public String getRowLabel() { return rowLabel; }
    public int getSeat_number() { return seat_number; }
    public SeatType getType() { return type; }
    public SeatStatus getStatus() { return status; }
    public void setStatus(SeatStatus status) { this.status = status; }
    public int getPriceCents() { return priceCents; }
    public String getDisplayId() {
        return rowLabel + "-" + String.format("%02d", seat_number); // For UI (like "A-12")
    }


    //Overriding Object methods
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Seat s)) return false;
        return Objects.equals(eventId, s.eventId)
                && Objects.equals(rowLabel, s.rowLabel)
                && seat_number == s.seat_number;
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, rowLabel, seat_number);
    }


    @Override
    public String toString() {
        return getDisplayId() + " (" + type + ")";
    }
}
