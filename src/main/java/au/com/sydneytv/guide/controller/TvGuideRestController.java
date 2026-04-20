package au.com.sydneytv.guide.controller;

import au.com.sydneytv.guide.model.DailyGuide;
import au.com.sydneytv.guide.model.Highlight;
import au.com.sydneytv.guide.service.TvGuideService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/guide")
public class TvGuideRestController {

    private final TvGuideService tvGuideService;

    public TvGuideRestController(TvGuideService tvGuideService) {
        this.tvGuideService = tvGuideService;
    }

    @GetMapping("/today")
    public DailyGuide today() {
        return tvGuideService.guideForToday();
    }

    @GetMapping("/week")
    public List<DailyGuide> week() {
        return tvGuideService.weeklyGuide();
    }

    @GetMapping("/day/{day}")
    public ResponseEntity<DailyGuide> forDay(@PathVariable String day) {
        DayOfWeek parsed;
        try {
            parsed = DayOfWeek.valueOf(day.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(tvGuideService.guideFor(parsed));
    }

    @GetMapping("/highlights/{day}")
    public ResponseEntity<List<Highlight>> highlights(@PathVariable String day) {
        DayOfWeek parsed;
        try {
            parsed = DayOfWeek.valueOf(day.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(tvGuideService.guideFor(parsed).getHighlights());
    }
}
