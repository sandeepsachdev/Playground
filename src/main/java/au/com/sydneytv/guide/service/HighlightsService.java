package au.com.sydneytv.guide.service;

import au.com.sydneytv.guide.model.Channel;
import au.com.sydneytv.guide.model.Highlight;
import au.com.sydneytv.guide.model.Program;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Picks prime-time (6pm-10pm) highlights using a simple, transparent scoring model.
 * The goal is to surface a handful of "must-watch" picks per evening across the five
 * Sydney free-to-air networks.
 */
@Service
public class HighlightsService {

    private static final LocalTime WINDOW_START = TvGuideService.PRIME_TIME_START;
    private static final LocalTime WINDOW_END = TvGuideService.PRIME_TIME_END;
    private static final int MAX_HIGHLIGHTS = 5;

    private static final Set<String> PREMIUM_GENRES = Set.of(
            "Drama", "Movie", "Sport", "Documentary", "Comedy"
    );

    private static final Set<String> FLAGSHIP_TITLES = Set.of(
            "Four Corners",
            "60 Minutes",
            "Australian Story",
            "MasterChef Australia",
            "Have You Been Paying Attention?",
            "Gruen",
            "Hard Quiz",
            "Lego Masters",
            "Fisk",
            "The Cheap Seats",
            "Call the Midwife"
    );

    public List<Highlight> pickHighlights(List<Channel> channels) {
        List<ScoredProgram> candidates = new ArrayList<>();
        for (Channel channel : channels) {
            for (Program program : channel.getPrograms()) {
                if (!program.overlapsWith(WINDOW_START, WINDOW_END)) {
                    continue;
                }
                int score = score(program);
                if (score <= 0) {
                    continue;
                }
                candidates.add(new ScoredProgram(channel, program, score, reason(program)));
            }
        }

        return candidates.stream()
                .sorted(Comparator
                        .comparingInt(ScoredProgram::score).reversed()
                        .thenComparing(sp -> sp.program().getStartTime()))
                .limit(MAX_HIGHLIGHTS)
                .sorted(Comparator.comparing(sp -> sp.program().getStartTime()))
                .map(sp -> new Highlight(
                        sp.channel().getName(),
                        sp.program().getStartTime(),
                        sp.program().getTitle(),
                        sp.reason()))
                .toList();
    }

    private int score(Program program) {
        int score = 0;
        if (FLAGSHIP_TITLES.contains(program.getTitle())) {
            score += 5;
        }
        if (PREMIUM_GENRES.contains(program.getGenre())) {
            score += 3;
        }
        if ("Sport".equalsIgnoreCase(program.getGenre())) {
            score += 1;
        }
        if ("Movie".equalsIgnoreCase(program.getGenre())) {
            score += 2;
        }
        if (isNewsOrCurrentAffairsFiller(program)) {
            score -= 2;
        }
        return score;
    }

    private boolean isNewsOrCurrentAffairsFiller(Program program) {
        String title = program.getTitle().toLowerCase();
        return title.contains("news") && !title.contains("late")
                || title.startsWith("a current affair")
                || title.startsWith("the project");
    }

    private String reason(Program program) {
        if (FLAGSHIP_TITLES.contains(program.getTitle())) {
            return "Flagship Aussie favourite — a reliably strong pick.";
        }
        return switch (program.getGenre()) {
            case "Movie" -> "Feature film premiere worth planning your evening around.";
            case "Sport" -> "Live sport, best watched as it happens.";
            case "Drama" -> "Quality drama to settle in with.";
            case "Documentary" -> "Thought-provoking doco for a slower evening.";
            case "Comedy" -> "A laugh-out-loud pick to unwind with.";
            default -> "Standout choice in tonight's line-up.";
        };
    }

    private record ScoredProgram(Channel channel, Program program, int score, String reason) {
    }
}
