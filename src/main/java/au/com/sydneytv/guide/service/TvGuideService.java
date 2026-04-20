package au.com.sydneytv.guide.service;

import au.com.sydneytv.guide.model.Channel;
import au.com.sydneytv.guide.model.DailyGuide;
import au.com.sydneytv.guide.model.Highlight;
import au.com.sydneytv.guide.model.Program;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class TvGuideService {

    public static final LocalTime PRIME_TIME_START = LocalTime.of(18, 0);
    public static final LocalTime PRIME_TIME_END = LocalTime.of(22, 0);
    public static final ZoneId SYDNEY = ZoneId.of("Australia/Sydney");
    public static final int WINDOW_DAYS = 7;

    private final EpgService epgService;
    private final HighlightsService highlightsService;

    public TvGuideService(EpgService epgService, HighlightsService highlightsService) {
        this.epgService = epgService;
        this.highlightsService = highlightsService;
    }

    public DailyGuide guideFor(LocalDate date) {
        List<Channel> channels = epgService.scheduleFor(date);
        List<Highlight> highlights = highlightsService.pickHighlights(channels);
        return new DailyGuide(date, channels, highlights);
    }

    public DailyGuide guideForToday() {
        return guideFor(today());
    }

    public List<DailyGuide> weeklyGuide() {
        LocalDate today = today();
        return IntStream.range(0, WINDOW_DAYS)
                .mapToObj(today::plusDays)
                .map(this::guideFor)
                .toList();
    }

    public List<LocalDate> guideDates() {
        LocalDate today = today();
        return IntStream.range(0, WINDOW_DAYS)
                .mapToObj(today::plusDays)
                .toList();
    }

    public LocalDate today() {
        return LocalDate.now(SYDNEY);
    }

    public List<Program> primeTimeProgramsFor(LocalDate date, String channelNumber) {
        return epgService.scheduleFor(date).stream()
                .filter(channel -> channel.getNumber().equals(channelNumber))
                .flatMap(channel -> channel.getPrograms().stream())
                .filter(program -> program.overlapsWith(PRIME_TIME_START, PRIME_TIME_END))
                .toList();
    }
}
