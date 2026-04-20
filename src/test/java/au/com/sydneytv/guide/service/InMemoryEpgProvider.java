package au.com.sydneytv.guide.service;

import au.com.sydneytv.guide.model.Channel;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Tiny test-only EPG provider. Constructed with whatever schedule the test needs. */
class InMemoryEpgProvider implements EpgProvider {

    private final Map<LocalDate, List<Channel>> schedule;

    InMemoryEpgProvider(Map<LocalDate, List<Channel>> schedule) {
        this.schedule = schedule;
    }

    @Override
    public String name() {
        return "in-memory";
    }

    @Override
    public Map<LocalDate, List<Channel>> fetchSchedule() {
        return schedule;
    }
}
