package au.com.sydneytv.guide.service;

import au.com.sydneytv.guide.config.EpgProperties;
import au.com.sydneytv.guide.config.EpgProperties.ChannelMapping;
import au.com.sydneytv.guide.model.Channel;
import au.com.sydneytv.guide.model.Program;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class XmltvEpgProviderTest {

    private static final LocalDate MONDAY_20_APR = LocalDate.of(2026, 4, 20);

    private final EpgProperties properties = properties();
    private final XmltvEpgProvider provider = new XmltvEpgProvider(properties);

    @Test
    void parsesProgrammesAndBucketsBySydneyDate() throws Exception {
        Map<LocalDate, List<Channel>> schedule;
        try (InputStream stream = getClass().getResourceAsStream("/xmltv/sample-au.xml")) {
            assertThat(stream).isNotNull();
            schedule = provider.parse(stream);
        }

        assertThat(schedule).containsOnlyKeys(MONDAY_20_APR);
        List<Channel> monday = schedule.get(MONDAY_20_APR);
        assertThat(monday).hasSize(3);

        Channel abc = monday.stream().filter(c -> c.getNumber().equals("2")).findFirst().orElseThrow();
        assertThat(abc.getPrograms())
                .extracting(Program::getTitle)
                .containsExactly("ABC News", "Hard Quiz", "Four Corners");
        assertThat(abc.getPrograms().get(0).getStartTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(abc.getPrograms().get(2).getEndTime()).isEqualTo(LocalTime.of(21, 30));

        Channel seven = monday.stream().filter(c -> c.getNumber().equals("7")).findFirst().orElseThrow();
        assertThat(seven.getPrograms())
                .extracting(Program::getTitle)
                .containsExactly("My Kitchen Rules");
    }

    @Test
    void skipsUnmappedChannels() throws Exception {
        try (InputStream stream = getClass().getResourceAsStream("/xmltv/sample-au.xml")) {
            Map<LocalDate, List<Channel>> schedule = provider.parse(stream);
            for (List<Channel> channels : schedule.values()) {
                assertThat(channels).extracting(Channel::getNumber)
                        .doesNotContain("unmapped.example.com");
            }
        }
    }

    @Test
    void parseTimeHandlesStandardFormat() {
        assertThat(XmltvEpgProvider.parseTime("20260420193000 +1000"))
                .isNotNull()
                .satisfies(dt -> assertThat(dt.withZoneSameInstant(XmltvEpgProvider.SYDNEY)
                        .toLocalTime()).isEqualTo(LocalTime.of(19, 30)));
    }

    @Test
    void parseTimeReturnsNullForGarbage() {
        assertThat(XmltvEpgProvider.parseTime("not a date")).isNull();
    }

    @Test
    void fetchScheduleRequiresConfiguredUrl() {
        EpgProperties blank = new EpgProperties();
        blank.setChannels(List.of(new ChannelMapping("abc1.abc.net.au", "2", "ABC", "ABC")));
        XmltvEpgProvider noUrl = new XmltvEpgProvider(blank);
        assertThat(org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class, noUrl::fetchSchedule))
                .hasMessageContaining("xmltv-url");
    }

    private static EpgProperties properties() {
        EpgProperties p = new EpgProperties();
        p.setXmltvUrl("http://example.com/feed.xml");
        p.setChannels(List.of(
                new ChannelMapping("abc1.abc.net.au", "2", "ABC TV", "ABC"),
                new ChannelMapping("sbs-one.sbs.com.au", "3", "SBS", "SBS"),
                new ChannelMapping("seven.seven.com.au", "7", "Seven", "Seven Network")
        ));
        return p;
    }
}
