package au.com.sydneytv.guide.service;

import au.com.sydneytv.guide.model.Channel;
import au.com.sydneytv.guide.model.DailyGuide;
import au.com.sydneytv.guide.model.Highlight;
import au.com.sydneytv.guide.model.Program;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TvGuideService {

    public static final LocalTime PRIME_TIME_START = LocalTime.of(18, 0);
    public static final LocalTime PRIME_TIME_END = LocalTime.of(22, 0);

    private final ScheduleRepository scheduleRepository;
    private final HighlightsService highlightsService;

    public TvGuideService(ScheduleRepository scheduleRepository, HighlightsService highlightsService) {
        this.scheduleRepository = scheduleRepository;
        this.highlightsService = highlightsService;
    }

    public DailyGuide guideFor(DayOfWeek day) {
        List<Channel> channels = scheduleRepository.scheduleFor(day);
        List<Highlight> highlights = highlightsService.pickHighlights(channels);
        return new DailyGuide(day, channels, highlights);
    }

    public DailyGuide guideForToday() {
        return guideFor(LocalDate.now().getDayOfWeek());
    }

    public List<DailyGuide> weeklyGuide() {
        List<DailyGuide> week = new ArrayList<>(7);
        for (DayOfWeek day : DayOfWeek.values()) {
            week.add(guideFor(day));
        }
        return week;
    }

    public List<Program> primeTimeProgramsFor(DayOfWeek day, String channelNumber) {
        return scheduleRepository.scheduleFor(day).stream()
                .filter(channel -> channel.getNumber().equals(channelNumber))
                .flatMap(channel -> channel.getPrograms().stream())
                .filter(program -> program.overlapsWith(PRIME_TIME_START, PRIME_TIME_END))
                .toList();
    }
}
