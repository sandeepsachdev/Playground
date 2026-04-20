package au.com.sydneytv.guide.service;

import au.com.sydneytv.guide.model.Channel;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Returned when no live EPG feed is configured. Produces no data so the UI
 * renders an explicit empty state rather than misleading sample content.
 */
public class EmptyEpgProvider implements EpgProvider {

    @Override
    public String name() {
        return "empty (no live feed configured)";
    }

    @Override
    public Map<LocalDate, List<Channel>> fetchSchedule() {
        return Map.of();
    }
}
