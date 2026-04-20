package au.com.sydneytv.guide.service;

import au.com.sydneytv.guide.model.Channel;
import au.com.sydneytv.guide.model.DailyGuide;
import au.com.sydneytv.guide.model.Highlight;
import au.com.sydneytv.guide.model.Program;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TvGuideServiceTest {

    private final TvGuideService service = new TvGuideService(new ScheduleRepository(), new HighlightsService());

    @Test
    void everyDayHasAllFiveNetworks() {
        for (DayOfWeek day : DayOfWeek.values()) {
            DailyGuide guide = service.guideFor(day);
            assertThat(guide.getChannels())
                    .as("channels for %s", day)
                    .extracting(Channel::getNumber)
                    .containsExactlyInAnyOrder("2", "3", "7", "9", "10");
        }
    }

    @Test
    void allPrimeTimeProgramsFallInsideSixToTen() {
        LocalTime windowStart = LocalTime.of(18, 0);
        LocalTime windowEnd = LocalTime.of(22, 0);

        for (DayOfWeek day : DayOfWeek.values()) {
            for (Channel channel : service.guideFor(day).getChannels()) {
                for (Program program : channel.getPrograms()) {
                    assertThat(program.getStartTime()).isBetween(windowStart, windowEnd);
                    assertThat(program.getEndTime()).isBetween(windowStart, windowEnd.plusMinutes(1));
                }
            }
        }
    }

    @Test
    void highlightsAreReturnedForEachDay() {
        for (DayOfWeek day : DayOfWeek.values()) {
            List<Highlight> highlights = service.guideFor(day).getHighlights();
            assertThat(highlights)
                    .as("highlights for %s", day)
                    .isNotEmpty()
                    .hasSizeLessThanOrEqualTo(5);
            assertThat(highlights)
                    .allSatisfy(h -> {
                        assertThat(h.getStartTime()).isBetween(LocalTime.of(18, 0), LocalTime.of(22, 0));
                        assertThat(h.getReason()).isNotBlank();
                        assertThat(h.getTitle()).isNotBlank();
                        assertThat(h.getChannel()).isNotBlank();
                    });
        }
    }

    @Test
    void highlightsAreOrderedByStartTime() {
        List<Highlight> highlights = service.guideFor(DayOfWeek.MONDAY).getHighlights();
        assertThat(highlights).isSortedAccordingTo((a, b) -> a.getStartTime().compareTo(b.getStartTime()));
    }

    @Test
    void weeklyGuideHasSevenEntries() {
        assertThat(service.weeklyGuide()).hasSize(7);
    }

    @Test
    void primeTimeProgramsFilterByChannel() {
        List<Program> abcMonday = service.primeTimeProgramsFor(DayOfWeek.MONDAY, "2");
        assertThat(abcMonday).isNotEmpty();
        assertThat(abcMonday).extracting(Program::getTitle).contains("Four Corners");
    }
}
