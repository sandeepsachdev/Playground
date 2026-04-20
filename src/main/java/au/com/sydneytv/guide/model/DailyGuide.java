package au.com.sydneytv.guide.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class DailyGuide {

    private static final DateTimeFormatter LONG = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter SHORT = DateTimeFormatter.ofPattern("EEE d MMM", Locale.ENGLISH);

    private final LocalDate date;
    private final List<Channel> channels;
    private final List<Highlight> highlights;

    public DailyGuide(LocalDate date, List<Channel> channels, List<Highlight> highlights) {
        this.date = date;
        this.channels = channels;
        this.highlights = highlights;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getLongLabel() {
        return LONG.format(date);
    }

    public String getShortLabel() {
        return SHORT.format(date);
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public List<Highlight> getHighlights() {
        return highlights;
    }

    public boolean isEmpty() {
        return channels.stream().allMatch(c -> c.getPrograms().isEmpty());
    }
}
