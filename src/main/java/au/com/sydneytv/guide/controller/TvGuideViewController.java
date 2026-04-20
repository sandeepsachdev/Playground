package au.com.sydneytv.guide.controller;

import au.com.sydneytv.guide.service.TvGuideService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Locale;

@Controller
public class TvGuideViewController {

    private final TvGuideService tvGuideService;

    public TvGuideViewController(TvGuideService tvGuideService) {
        this.tvGuideService = tvGuideService;
    }

    @GetMapping("/")
    public String home(@RequestParam(value = "day", required = false) String day, Model model) {
        DayOfWeek selected = parseDay(day);
        model.addAttribute("guide", tvGuideService.guideFor(selected));
        model.addAttribute("days", Arrays.asList(DayOfWeek.values()));
        model.addAttribute("selectedDay", selected);
        return "guide";
    }

    private DayOfWeek parseDay(String day) {
        if (day == null || day.isBlank()) {
            return LocalDate.now().getDayOfWeek();
        }
        try {
            return DayOfWeek.valueOf(day.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return LocalDate.now().getDayOfWeek();
        }
    }
}
