package au.com.sydneytv.guide.model;

import java.time.DayOfWeek;
import java.util.List;

public class DailyGuide {

    private final DayOfWeek day;
    private final List<Channel> channels;
    private final List<Highlight> highlights;

    public DailyGuide(DayOfWeek day, List<Channel> channels, List<Highlight> highlights) {
        this.day = day;
        this.channels = channels;
        this.highlights = highlights;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public String getDayName() {
        return day.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH);
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public List<Highlight> getHighlights() {
        return highlights;
    }
}
