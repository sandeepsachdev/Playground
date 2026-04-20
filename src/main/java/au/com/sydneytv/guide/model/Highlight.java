package au.com.sydneytv.guide.model;

import java.time.LocalTime;

public class Highlight {

    private final String channel;
    private final LocalTime startTime;
    private final String title;
    private final String reason;

    public Highlight(String channel, LocalTime startTime, String title, String reason) {
        this.channel = channel;
        this.startTime = startTime;
        this.title = title;
        this.reason = reason;
    }

    public String getChannel() {
        return channel;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public String getTitle() {
        return title;
    }

    public String getReason() {
        return reason;
    }
}
