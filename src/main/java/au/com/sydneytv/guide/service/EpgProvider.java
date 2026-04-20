package au.com.sydneytv.guide.service;

import au.com.sydneytv.guide.model.Channel;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;

/**
 * A source of Sydney free-to-air EPG data. Implementations may serve static
 * content, scrape a schedule site, or adapt a commercial feed.
 */
public interface EpgProvider {

    /**
     * @return a short name used in logs and diagnostics.
     */
    String name();

    /**
     * Fetch the current schedule bucketed by day of week (Sydney local time).
     * Implementations should throw if the upstream source is unreachable so
     * callers can fall back to another provider.
     */
    Map<DayOfWeek, List<Channel>> fetchSchedule();
}
