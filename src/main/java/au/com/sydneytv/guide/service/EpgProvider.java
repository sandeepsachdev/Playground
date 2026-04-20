package au.com.sydneytv.guide.service;

import au.com.sydneytv.guide.model.Channel;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * A source of live Sydney free-to-air EPG data.
 */
public interface EpgProvider {

    /**
     * @return a short name used in logs and diagnostics.
     */
    String name();

    /**
     * Fetch the current schedule bucketed by Sydney-local date.
     * Implementations should throw if the upstream source is unreachable.
     */
    Map<LocalDate, List<Channel>> fetchSchedule();
}
