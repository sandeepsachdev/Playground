package au.com.sydneytv.guide.controller;

import au.com.sydneytv.guide.model.DailyGuide;
import au.com.sydneytv.guide.model.Highlight;
import au.com.sydneytv.guide.service.TvGuideService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

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

    @GetMapping("/date/{date}")
    public DailyGuide forDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return tvGuideService.guideFor(date);
    }

    @GetMapping("/highlights/{date}")
    public List<Highlight> highlights(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return tvGuideService.guideFor(date).getHighlights();
    }
}
