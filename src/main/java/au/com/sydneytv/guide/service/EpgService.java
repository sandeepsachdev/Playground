package au.com.sydneytv.guide.service;

import au.com.sydneytv.guide.config.EpgProperties;
import au.com.sydneytv.guide.model.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Caches the most recent successful EPG fetch. If the configured provider
 * fails (or none is configured), returns the last known cache or an empty
 * schedule — the UI is expected to render an explicit empty state rather than
 * fall back to sample data.
 */
@Service
public class EpgService {

    private static final Logger log = LoggerFactory.getLogger(EpgService.class);

    private final EpgProvider provider;
    private final Duration refreshInterval;

    private volatile Map<LocalDate, List<Channel>> cachedSchedule = Map.of();
    private volatile Instant cachedAt;

    public EpgService(EpgProvider provider, EpgProperties properties) {
        this.provider = provider;
        this.refreshInterval = properties.getRefreshInterval();
    }

    public List<Channel> scheduleFor(LocalDate date) {
        return getSchedule().getOrDefault(date, List.of());
    }

    public Set<LocalDate> availableDates() {
        return new TreeSet<>(getSchedule().keySet());
    }

    public String activeProviderName() {
        return provider.name();
    }

    private Map<LocalDate, List<Channel>> getSchedule() {
        if (cachedAt != null && !isExpired()) {
            return cachedSchedule;
        }
        return refresh();
    }

    private synchronized Map<LocalDate, List<Channel>> refresh() {
        if (cachedAt != null && !isExpired()) {
            return cachedSchedule;
        }
        try {
            Map<LocalDate, List<Channel>> fresh = provider.fetchSchedule();
            cachedSchedule = fresh;
            cachedAt = Instant.now();
            log.info("Loaded EPG from provider '{}' ({} dates, cache TTL {})",
                    provider.name(), fresh.size(), refreshInterval);
            return fresh;
        } catch (RuntimeException ex) {
            log.warn("EPG provider '{}' failed: {}. Returning {}.",
                    provider.name(), ex.getMessage(),
                    cachedSchedule.isEmpty() ? "empty schedule" : "stale cache");
            cachedAt = Instant.now();
            return cachedSchedule;
        }
    }

    private boolean isExpired() {
        return cachedAt == null
                || Duration.between(cachedAt, Instant.now()).compareTo(refreshInterval) > 0;
    }
}
