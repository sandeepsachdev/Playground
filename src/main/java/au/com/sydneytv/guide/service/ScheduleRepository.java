package au.com.sydneytv.guide.service;

import au.com.sydneytv.guide.model.Channel;
import au.com.sydneytv.guide.model.Program;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory fallback schedule for Sydney free-to-air networks. Times are Sydney
 * local time. Used when no live EPG source is configured (or one is configured
 * but unreachable).
 */
public class ScheduleRepository implements EpgProvider {

    private final Map<DayOfWeek, List<Channel>> weeklySchedule = new EnumMap<>(DayOfWeek.class);

    public ScheduleRepository() {
        for (DayOfWeek day : DayOfWeek.values()) {
            weeklySchedule.put(day, buildSchedule(day));
        }
    }

    @Override
    public String name() {
        return "static";
    }

    @Override
    public Map<DayOfWeek, List<Channel>> fetchSchedule() {
        return weeklySchedule;
    }

    public List<Channel> scheduleFor(DayOfWeek day) {
        return weeklySchedule.get(day);
    }

    private List<Channel> buildSchedule(DayOfWeek day) {
        return List.of(
                abc(day),
                sbs(day),
                seven(day),
                nine(day),
                ten(day)
        );
    }

    private Channel abc(DayOfWeek day) {
        return new Channel("2", "ABC TV", "ABC", switch (day) {
            case MONDAY -> List.of(
                    program(18, 0, 19, 0, "ABC News", "News", "NC", "National and Sydney news with Juanita Phillips."),
                    program(19, 0, 19, 30, "7.30", "Current Affairs", "PG", "Sarah Ferguson with the day's major stories."),
                    program(19, 30, 20, 0, "Hard Quiz", "Comedy/Quiz", "PG", "Tom Gleeson grills specialist subject contestants."),
                    program(20, 0, 20, 30, "Australian Story", "Documentary", "PG", "In-depth profile of a remarkable Australian."),
                    program(20, 30, 21, 30, "Four Corners", "Current Affairs", "M", "Investigative journalism flagship."),
                    program(21, 30, 22, 0, "Media Watch", "Current Affairs", "PG", "A weekly look at the media.")
            );
            case TUESDAY -> List.of(
                    program(18, 0, 19, 0, "ABC News", "News", "NC", "National and Sydney news."),
                    program(19, 0, 19, 30, "7.30", "Current Affairs", "PG", "Nightly current affairs programme."),
                    program(19, 30, 20, 30, "Foreign Correspondent", "Documentary", "PG", "Global stories from ABC correspondents."),
                    program(20, 30, 21, 25, "Gruen", "Comedy", "M", "Wil Anderson and the panel dissect advertising."),
                    program(21, 25, 22, 0, "Planet America", "Current Affairs", "PG", "Chas and John on US politics.")
            );
            case WEDNESDAY -> List.of(
                    program(18, 0, 19, 0, "ABC News", "News", "NC", "National and Sydney news."),
                    program(19, 0, 19, 30, "7.30", "Current Affairs", "PG", "Nightly current affairs."),
                    program(19, 30, 20, 30, "Catalyst", "Science", "PG", "Science stories that shape our world."),
                    program(20, 30, 21, 30, "Question Everything", "Comedy", "M", "Wil Anderson and panel dissect media and misinformation."),
                    program(21, 30, 22, 0, "The Weekly with Charlie Pickering", "Comedy", "M", "Satirical look at the week in news.")
            );
            case THURSDAY -> List.of(
                    program(18, 0, 19, 0, "ABC News", "News", "NC", "National and Sydney news."),
                    program(19, 0, 19, 30, "7.30", "Current Affairs", "PG", "Nightly current affairs."),
                    program(19, 30, 20, 30, "Grand Designs Australia", "Lifestyle", "PG", "Ambitious Aussie builds with Anthony Burke."),
                    program(20, 30, 21, 30, "Fisk", "Comedy", "M", "Kitty Flanagan returns as probate lawyer Helen Tudor-Fisk."),
                    program(21, 30, 22, 0, "Hard Quiz Encore", "Comedy/Quiz", "PG", "Repeat of Monday's episode.")
            );
            case FRIDAY -> List.of(
                    program(18, 0, 19, 0, "ABC News", "News", "NC", "National and Sydney news."),
                    program(19, 0, 19, 30, "7.30", "Current Affairs", "PG", "Friday edition with a politics focus."),
                    program(19, 30, 20, 30, "Gardening Australia", "Lifestyle", "G", "Costa Georgiadis and the team."),
                    program(20, 30, 22, 0, "Death in Paradise", "Drama", "M", "British mystery set in the Caribbean.")
            );
            case SATURDAY -> List.of(
                    program(18, 0, 19, 0, "ABC News", "News", "NC", "National and Sydney news."),
                    program(19, 0, 20, 0, "Back Roads", "Documentary", "PG", "Heather Ewart explores regional Australia."),
                    program(20, 0, 21, 30, "Midsomer Murders", "Drama", "M", "DCI Barnaby solves a bucolic murder."),
                    program(21, 30, 22, 0, "Mother and Son", "Comedy", "PG", "Matt Okine as Arthur Beare.")
            );
            case SUNDAY -> List.of(
                    program(18, 0, 19, 0, "ABC News Sunday", "News", "NC", "The week's news with David Speers."),
                    program(19, 0, 19, 40, "Compass", "Documentary", "PG", "Stories of faith, ethics and values."),
                    program(19, 40, 20, 30, "Landline Sunday", "Lifestyle", "PG", "Rural and regional affairs."),
                    program(20, 30, 21, 30, "Call the Midwife", "Drama", "M", "Drama set in 1960s East London."),
                    program(21, 30, 22, 0, "Grand Designs Revisited", "Lifestyle", "PG", "Kevin McCloud revisits past builds.")
            );
        });
    }

    private Channel sbs(DayOfWeek day) {
        return new Channel("3", "SBS", "SBS", switch (day) {
            case MONDAY -> List.of(
                    program(18, 0, 18, 30, "Mastermind Australia", "Quiz", "G", "Marc Fennell hosts the iconic quiz."),
                    program(18, 30, 19, 30, "SBS World News", "News", "NC", "International news with Janice Petersen."),
                    program(19, 30, 20, 30, "Who Do You Think You Are?", "Documentary", "PG", "Celebrities trace their family tree."),
                    program(20, 30, 21, 30, "Great Australian Walks with Julia Zemiro", "Travel", "PG", "Julia walks iconic Australian trails."),
                    program(21, 30, 22, 0, "Dateline", "Current Affairs", "M", "Global current affairs.")
            );
            case TUESDAY -> List.of(
                    program(18, 0, 18, 30, "Mastermind Australia", "Quiz", "G", "Marc Fennell hosts."),
                    program(18, 30, 19, 30, "SBS World News", "News", "NC", "International news."),
                    program(19, 30, 20, 30, "Insight", "Current Affairs", "M", "Kumi Taguchi hosts discussion on topical issues."),
                    program(20, 30, 21, 30, "Great British Railway Journeys", "Travel", "G", "Michael Portillo's rail adventures."),
                    program(21, 30, 22, 0, "The Cook Up with Adam Liaw", "Lifestyle", "G", "Quick weeknight cooking ideas.")
            );
            case WEDNESDAY -> List.of(
                    program(18, 0, 18, 30, "Mastermind Australia", "Quiz", "G", "Marc Fennell hosts."),
                    program(18, 30, 19, 30, "SBS World News", "News", "NC", "International news."),
                    program(19, 30, 20, 30, "Every Family Has a Secret", "Documentary", "PG", "Noni Hazlehurst uncovers hidden family histories."),
                    program(20, 30, 21, 30, "24 Hours in Emergency", "Documentary", "M", "Inside a London A&E."),
                    program(21, 30, 22, 0, "SBS World News Late", "News", "NC", "Late bulletin.")
            );
            case THURSDAY -> List.of(
                    program(18, 0, 18, 30, "Mastermind Australia", "Quiz", "G", "Marc Fennell hosts."),
                    program(18, 30, 19, 30, "SBS World News", "News", "NC", "International news."),
                    program(19, 30, 20, 30, "Such Was Life", "Documentary", "PG", "Untold stories from Australia's past."),
                    program(20, 30, 22, 0, "Vera", "Drama", "M", "Brenda Blethyn as DCI Vera Stanhope.")
            );
            case FRIDAY -> List.of(
                    program(18, 0, 18, 30, "Mastermind Australia", "Quiz", "G", "Marc Fennell hosts."),
                    program(18, 30, 19, 30, "SBS World News", "News", "NC", "International news."),
                    program(19, 30, 20, 30, "Great Continental Railway Journeys", "Travel", "G", "Michael Portillo travels Europe."),
                    program(20, 30, 22, 0, "SBS World Movies Premiere", "Movie", "M", "An acclaimed international film.")
            );
            case SATURDAY -> List.of(
                    program(18, 0, 18, 30, "SBS World News", "News", "NC", "Evening bulletin."),
                    program(18, 30, 19, 30, "World Watch: Europe", "News", "NC", "News from European broadcasters."),
                    program(19, 30, 20, 30, "RocKwiz", "Music", "PG", "Julia Zemiro hosts the rock and roll quiz."),
                    program(20, 30, 22, 0, "Saturday Night Movie", "Movie", "M", "Premiere film from SBS World Movies.")
            );
            case SUNDAY -> List.of(
                    program(18, 0, 18, 30, "SBS World News", "News", "NC", "Evening bulletin."),
                    program(18, 30, 19, 30, "Lost for Words", "Documentary", "PG", "Adult literacy journey in Australia."),
                    program(19, 30, 20, 30, "Tony Robinson's Museum Secrets", "Documentary", "PG", "Behind the scenes of famous museums."),
                    program(20, 30, 21, 30, "Miniseries: The Tourist", "Drama", "MA15+", "Jamie Dornan in the BBC thriller."),
                    program(21, 30, 22, 0, "Dateline", "Current Affairs", "M", "Global current affairs.")
            );
        });
    }

    private Channel seven(DayOfWeek day) {
        return new Channel("7", "Seven", "Seven Network", switch (day) {
            case MONDAY -> List.of(
                    program(18, 0, 19, 0, "Seven News", "News", "NC", "Sydney news with Michael Usher."),
                    program(19, 0, 19, 30, "Home and Away", "Drama", "PG", "Ongoing saga in Summer Bay."),
                    program(19, 30, 20, 30, "The Chase Australia Celebrity Special", "Quiz", "PG", "Larry Emdur hosts celebrity chasers."),
                    program(20, 30, 21, 30, "RFDS", "Drama", "M", "Royal Flying Doctor Service drama."),
                    program(21, 30, 22, 0, "7News Spotlight", "Current Affairs", "M", "In-depth investigations.")
            );
            case TUESDAY -> List.of(
                    program(18, 0, 19, 0, "Seven News", "News", "NC", "Sydney news."),
                    program(19, 0, 19, 30, "Home and Away", "Drama", "PG", "Ongoing saga in Summer Bay."),
                    program(19, 30, 21, 0, "My Kitchen Rules", "Reality", "PG", "Manu Feildel and Colin Fassnidge judge home cooks."),
                    program(21, 0, 22, 0, "The Front Bar", "Sport/Comedy", "M", "AFL chat with Mick Molloy.")
            );
            case WEDNESDAY -> List.of(
                    program(18, 0, 19, 0, "Seven News", "News", "NC", "Sydney news."),
                    program(19, 0, 19, 30, "Home and Away", "Drama", "PG", "Ongoing saga in Summer Bay."),
                    program(19, 30, 21, 0, "My Kitchen Rules", "Reality", "PG", "Instant restaurant round continues."),
                    program(21, 0, 22, 0, "FBI", "Drama", "M", "Procedural crime drama.")
            );
            case THURSDAY -> List.of(
                    program(18, 0, 19, 0, "Seven News", "News", "NC", "Sydney news."),
                    program(19, 0, 19, 30, "Home and Away", "Drama", "PG", "Ongoing saga in Summer Bay."),
                    program(19, 30, 21, 30, "AFL: Thursday Night Football", "Sport", "NC", "Live AFL coverage."),
                    program(21, 30, 22, 0, "Armchair Experts", "Sport", "PG", "AFL wrap with Hamish McLachlan.")
            );
            case FRIDAY -> List.of(
                    program(18, 0, 19, 0, "Seven News", "News", "NC", "Sydney news."),
                    program(19, 0, 19, 30, "Better Homes and Gardens", "Lifestyle", "G", "Johanna Griggs and team."),
                    program(19, 30, 21, 30, "AFL Friday Night Football", "Sport", "NC", "Live AFL."),
                    program(21, 30, 22, 0, "Movie: Premiere", "Movie", "M", "Friday night blockbuster.")
            );
            case SATURDAY -> List.of(
                    program(18, 0, 19, 0, "Seven News", "News", "NC", "Weekend bulletin."),
                    program(19, 0, 19, 30, "Border Security: Australia's Front Line", "Documentary", "PG", "Customs and border force."),
                    program(19, 30, 22, 0, "AFL Saturday Night Football", "Sport", "NC", "Live AFL double-header.")
            );
            case SUNDAY -> List.of(
                    program(18, 0, 19, 0, "Seven News", "News", "NC", "Weekend bulletin with Mark Ferguson."),
                    program(19, 0, 20, 0, "Sunday Night Takeaway", "Variety", "PG", "Ant and Dec entertainment showcase."),
                    program(20, 0, 21, 30, "Sunday Night Movie", "Movie", "M", "Family feature film."),
                    program(21, 30, 22, 0, "Autopsy USA", "Documentary", "M", "Celebrity cause-of-death investigations.")
            );
        });
    }

    private Channel nine(DayOfWeek day) {
        return new Channel("9", "Nine", "Nine Network", switch (day) {
            case MONDAY -> List.of(
                    program(18, 0, 19, 0, "9News Sydney", "News", "NC", "Peter Overton with the day's news."),
                    program(19, 0, 19, 30, "A Current Affair", "Current Affairs", "PG", "Ally Langdon with topical stories."),
                    program(19, 30, 21, 0, "Married at First Sight", "Reality", "M", "Experts match couples for a social experiment."),
                    program(21, 0, 22, 0, "Under Investigation with Liz Hayes", "Current Affairs", "M", "Deep dive into cold cases.")
            );
            case TUESDAY -> List.of(
                    program(18, 0, 19, 0, "9News Sydney", "News", "NC", "Peter Overton."),
                    program(19, 0, 19, 30, "A Current Affair", "Current Affairs", "PG", "Topical stories."),
                    program(19, 30, 21, 0, "Married at First Sight", "Reality", "M", "Dinner party drama."),
                    program(21, 0, 22, 0, "Paramedics", "Documentary", "M", "Ride along with Victorian paramedics.")
            );
            case WEDNESDAY -> List.of(
                    program(18, 0, 19, 0, "9News Sydney", "News", "NC", "Peter Overton."),
                    program(19, 0, 19, 30, "A Current Affair", "Current Affairs", "PG", "Topical stories."),
                    program(19, 30, 21, 0, "Travel Guides", "Travel", "PG", "Five families rate holiday destinations."),
                    program(21, 0, 22, 0, "Footy Classified", "Sport", "M", "AFL panel with Caroline Wilson.")
            );
            case THURSDAY -> List.of(
                    program(18, 0, 19, 0, "9News Sydney", "News", "NC", "Peter Overton."),
                    program(19, 0, 19, 30, "A Current Affair", "Current Affairs", "PG", "Topical stories."),
                    program(19, 30, 20, 30, "RBT", "Documentary", "PG", "Random breath-testing stops with NSW Police."),
                    program(20, 30, 22, 0, "NRL Thursday Night Football", "Sport", "NC", "Live NRL match.")
            );
            case FRIDAY -> List.of(
                    program(18, 0, 19, 0, "9News Sydney", "News", "NC", "Peter Overton."),
                    program(19, 0, 19, 30, "A Current Affair", "Current Affairs", "PG", "Topical stories."),
                    program(19, 30, 22, 0, "NRL Friday Night Football", "Sport", "NC", "Live NRL double-header.")
            );
            case SATURDAY -> List.of(
                    program(18, 0, 19, 0, "9News Saturday", "News", "NC", "Weekend bulletin."),
                    program(19, 0, 19, 30, "A Current Affair Saturday", "Current Affairs", "PG", "Weekend wrap."),
                    program(19, 30, 22, 0, "NRL Saturday Night Football", "Sport", "NC", "Live NRL match coverage.")
            );
            case SUNDAY -> List.of(
                    program(18, 0, 19, 0, "9News Sunday", "News", "NC", "Sunday bulletin."),
                    program(19, 0, 20, 0, "60 Minutes", "Current Affairs", "M", "Flagship investigative journalism."),
                    program(20, 0, 21, 0, "Lego Masters", "Reality", "PG", "Hamish Blake hosts the building contest."),
                    program(21, 0, 22, 0, "See No Evil", "Documentary", "M", "Crimes solved with CCTV footage.")
            );
        });
    }

    private Channel ten(DayOfWeek day) {
        return new Channel("10", "10", "Network 10", switch (day) {
            case MONDAY -> List.of(
                    program(18, 0, 19, 0, "10 News First", "News", "NC", "Sydney news with Sandra Sully."),
                    program(19, 0, 19, 30, "The Project", "Current Affairs", "PG", "News and entertainment panel."),
                    program(19, 30, 21, 0, "MasterChef Australia", "Reality", "PG", "Andy, Sofia, Poh and Jean-Christophe judge."),
                    program(21, 0, 22, 0, "Have You Been Paying Attention?", "Comedy", "M", "Tom Gleisner's news quiz.")
            );
            case TUESDAY -> List.of(
                    program(18, 0, 19, 0, "10 News First", "News", "NC", "Sydney news."),
                    program(19, 0, 19, 30, "The Project", "Current Affairs", "PG", "News and entertainment panel."),
                    program(19, 30, 21, 0, "MasterChef Australia", "Reality", "PG", "Elimination round."),
                    program(21, 0, 22, 0, "NCIS", "Drama", "M", "Long-running naval crime procedural.")
            );
            case WEDNESDAY -> List.of(
                    program(18, 0, 19, 0, "10 News First", "News", "NC", "Sydney news."),
                    program(19, 0, 19, 30, "The Project", "Current Affairs", "PG", "News and entertainment panel."),
                    program(19, 30, 21, 0, "MasterChef Australia", "Reality", "PG", "Immunity challenge."),
                    program(21, 0, 22, 0, "The Cheap Seats", "Comedy", "M", "Melanie Bracewell and Tim McDonald on the week's news.")
            );
            case THURSDAY -> List.of(
                    program(18, 0, 19, 0, "10 News First", "News", "NC", "Sydney news."),
                    program(19, 0, 19, 30, "The Project", "Current Affairs", "PG", "News and entertainment panel."),
                    program(19, 30, 20, 30, "Gogglebox Australia", "Reality", "M", "Australian households react to the week's TV."),
                    program(20, 30, 21, 30, "Thank God You're Here", "Comedy", "M", "Celeste Barber and improv comedians."),
                    program(21, 30, 22, 0, "The Project Late", "Current Affairs", "PG", "Late edition.")
            );
            case FRIDAY -> List.of(
                    program(18, 0, 19, 0, "10 News First", "News", "NC", "Sydney news."),
                    program(19, 0, 19, 30, "The Project", "Current Affairs", "PG", "Friday edition."),
                    program(19, 30, 21, 0, "Gogglebox Australia", "Reality", "M", "Encore presentation."),
                    program(21, 0, 22, 0, "Taskmaster Australia", "Comedy", "M", "Tom Gleeson sets absurd tasks.")
            );
            case SATURDAY -> List.of(
                    program(18, 0, 19, 0, "10 News First", "News", "NC", "Weekend bulletin."),
                    program(19, 0, 20, 30, "Australian Survivor: Reunion", "Reality", "PG", "Jonathan LaPaglia revisits past seasons."),
                    program(20, 30, 22, 0, "Saturday Night Movie", "Movie", "M", "Feature film.")
            );
            case SUNDAY -> List.of(
                    program(18, 0, 19, 0, "10 News First", "News", "NC", "Weekend bulletin."),
                    program(19, 0, 19, 30, "The Sunday Project", "Current Affairs", "PG", "Hamish Macdonald hosts."),
                    program(19, 30, 21, 0, "The Dog House Australia", "Reality", "PG", "Matching rescue dogs with new owners."),
                    program(21, 0, 22, 0, "FBI: International", "Drama", "M", "Fly team based in Budapest.")
            );
        });
    }

    private static Program program(int startHour, int startMinute, int endHour, int endMinute,
                                   String title, String genre, String rating, String synopsis) {
        return new Program(
                LocalTime.of(startHour, startMinute),
                LocalTime.of(endHour, endMinute),
                title, genre, rating, synopsis
        );
    }
}
