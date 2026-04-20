package au.com.sydneytv.guide.model;

import java.time.LocalTime;

public class Program {

    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String title;
    private final String genre;
    private final String rating;
    private final String synopsis;

    public Program(LocalTime startTime, LocalTime endTime, String title,
                   String genre, String rating, String synopsis) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.title = title;
        this.genre = genre;
        this.rating = rating;
        this.synopsis = synopsis;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public String getRating() {
        return rating;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public boolean overlapsWith(LocalTime from, LocalTime to) {
        return startTime.isBefore(to) && endTime.isAfter(from);
    }
}
