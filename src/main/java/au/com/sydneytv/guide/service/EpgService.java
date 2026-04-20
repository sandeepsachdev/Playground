package au.com.sydneytv.guide.service;

import au.com.sydneytv.guide.config.EpgProperties;
import au.com.sydneytv.guide.model.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Routes schedule requests to the configured {@link EpgProvider}, caches the
 * most recent successful fetch, and falls back to the static schedule if a
 * live provider fails before any data has been loaded.
 */
@Service
public class EpgService {

    private static final Logger log = LoggerFactory.getLogger(EpgService.class);

    private final EpgProvider primary;
    private final EpgProvider fallback;
    private final Duration refreshInterval;

    private volatile Map<DayOfWeek, List<Channel>> cachedSchedule;
    private volatile Instant cachedAt;

    public EpgService(EpgProvider primary, ScheduleRepository fallback, EpgProperties properties) {
        this.primary = primary;
        this.fallback = fallback;
        this.refreshInterval = properties.getRefreshInterval();
    }

    public List<Channel> scheduleFor(DayOfWeek day) {
        return getSchedule().getOrDefault(day, List.of());
    }

    public String activeProviderName() {
        return primary.name();
    }

    private Map<DayOfWeek, List<Channel>> getSchedule() {
        Map<DayOfWeek, List<Channel>> snapshot = cachedSchedule;
        if (snapshot != null && !isExpired()) {
            return snapshot;
        }
        return refresh();
    }

    private synchronized Map<DayOfWeek, List<Channel>> refresh() {
        if (cachedSchedule != null && !isExpired()) {
            return cachedSchedule;
        }
        try {
            Map<DayOfWeek, List<Channel>> fresh = primary.fetchSchedule();
            cachedSchedule = fresh;
            cachedAt = Instant.now();
            log.info("Loaded EPG from provider '{}' (cache TTL {})", primary.name(), refreshInterval);
            return fresh;
        } catch (RuntimeException ex) {
            log.warn("EPG provider '{}' failed: {}. Falling back to '{}'.",
                    primary.name(), ex.getMessage(), fallback.name());
            if (cachedSchedule != null) {
                return cachedSchedule;
            }
            Map<DayOfWeek, List<Channel>> fallbackSchedule = fallback.fetchSchedule();
            cachedSchedule = fallbackSchedule;
            cachedAt = Instant.now();
            return fallbackSchedule;
        }
    }

    private boolean isExpired() {
        return cachedAt == null
                || Duration.between(cachedAt, Instant.now()).compareTo(refreshInterval) > 0;
    }
}
