package menuki.ticketing.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * This is the model class for event. This defines the basic structure and operations for an event
 */
public class Event {
    private String id;
    private String name;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String venue;
    private int seats;

   //Constructor
    public Event(String id, String name, LocalDate date, String venue, LocalTime startTime, LocalTime endTime, int seats) {
            this.id = id; this.name = name; this.date = date; this.venue = venue; this.startTime = startTime; this.endTime = endTime; this.seats = seats;
        }

    //Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public LocalDate getDate() { return date; }
    public String getVenue() { return venue; }
    public int getSeats() { return seats; }
    public LocalTime getStartTime() {return startTime;}
    public LocalTime getEndTime() {return endTime;}


    //Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setVenue(String venue) { this.venue = venue; }
    public void setSeats(int seats) { this.seats = seats; }
    public void setStartTime(LocalTime startTime) {this.startTime = startTime;}
    public void setEndTime(LocalTime endTime) {this.endTime = endTime;}
}
