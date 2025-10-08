package menuki.ticketing.service;

import menuki.ticketing.model.Event;
import java.util.List;

/**
 * Service layer contract for business logic related to events
 */
public interface EventService {
    void createEvent(Event event);
    List<Event> findAllEvents();
    String generateNextEventId();
    void createEventWithSeating(Event event, List<String> vipRows, int vipCents, int stdCents);
}
