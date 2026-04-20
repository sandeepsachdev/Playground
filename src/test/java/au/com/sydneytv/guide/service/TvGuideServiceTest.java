package au.com.sydneytv.guide.service;

import au.com.sydneytv.guide.config.EpgProperties;
import au.com.sydneytv.guide.config.EpgProperties.ChannelMapping;
import au.com.sydneytv.guide.model.Channel;
import au.com.sydneytv.guide.model.DailyGuide;
import au.com.sydneytv.guide.model.Highlight;
import au.com.sydneytv.guide.model.Program;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TvGuideServiceTest {

    private static final LocalDate FIXTURE_DATE = LocalDate.of(2026, 4, 20);

    @Test
    void buildsGuideFromLiveSchedule() throws Exception {
        TvGuideService service = serviceFromFixture();
        DailyGuide guide = service.guideFor(FIXTURE_DATE);

        assertThat(guide.getDate()).isEqualTo(FIXTURE_DATE);
        assertThat(guide.getLongLabel()).isEqualTo("Monday, 20 April 2026");
        assertThat(guide.isEmpty()).isFalse();
        assertThat(guide.getChannels())
                .extracting(Channel::getNumber)
                .containsExactlyInAnyOrder("2", "3", "7");
    }

    @Test
    void highlightsAreOrderedByStartTimeAndAnnotated() throws Exception {
        TvGuideService service = serviceFromFixture();
        List<Highlight> highlights = service.guideFor(FIXTURE_DATE).getHighlights();

        assertThat(highlights).isNotEmpty();
        assertThat(highlights).isSortedAccordingTo(
                (a, b) -> a.getStartTime().compareTo(b.getStartTime()));
        assertThat(highlights).allSatisfy(h -> {
            assertThat(h.getStartTime()).isBetween(LocalTime.of(18, 0), LocalTime.of(22, 0));
            assertThat(h.getReason()).isNotBlank();
        });
        assertThat(highlights).extracting(Highlight::getTitle)
                .contains("Four Corners", "Hard Quiz");
    }

    @Test
    void primeTimeProgramsFilterByChannel() throws Exception {
        TvGuideService service = serviceFromFixture();
        List<Program> abc = service.primeTimeProgramsFor(FIXTURE_DATE, "2");

        assertThat(abc).extracting(Program::getTitle)
                .contains("Four Corners");
    }

    @Test
    void emptyWhenNoFeedConfigured() {
        TvGuideService service = new TvGuideService(
                new EpgService(new EmptyEpgProvider(), new EpgProperties()),
                new HighlightsService());

        DailyGuide guide = service.guideFor(FIXTURE_DATE);
        assertThat(guide.isEmpty()).isTrue();
        assertThat(guide.getChannels()).isEmpty();
        assertThat(guide.getHighlights()).isEmpty();
    }

    @Test
    void weeklyGuideHasSevenConsecutiveDates() {
        TvGuideService service = new TvGuideService(
                new EpgService(new EmptyEpgProvider(), new EpgProperties()),
                new HighlightsService());

        List<DailyGuide> week = service.weeklyGuide();
        assertThat(week).hasSize(7);
        for (int i = 1; i < week.size(); i++) {
            assertThat(week.get(i).getDate())
                    .isEqualTo(week.get(i - 1).getDate().plusDays(1));
        }
    }

    private static TvGuideService serviceFromFixture() throws Exception {
        EpgProperties properties = new EpgProperties();
        properties.setXmltvUrl("http://example.com/feed.xml");
        properties.setChannels(List.of(
                new ChannelMapping("abc1.abc.net.au", "2", "ABC TV", "ABC"),
                new ChannelMapping("sbs-one.sbs.com.au", "3", "SBS", "SBS"),
                new ChannelMapping("seven.seven.com.au", "7", "Seven", "Seven Network")
        ));
        XmltvEpgProvider parser = new XmltvEpgProvider(properties);
        Map<LocalDate, List<Channel>> schedule;
        try (InputStream stream = TvGuideServiceTest.class
                .getResourceAsStream("/xmltv/sample-au.xml")) {
            schedule = parser.parse(stream);
        }
        return new TvGuideService(
                new EpgService(new InMemoryEpgProvider(schedule), properties),
                new HighlightsService());
    }
}
