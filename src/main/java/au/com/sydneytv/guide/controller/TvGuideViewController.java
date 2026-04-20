package au.com.sydneytv.guide.controller;

import au.com.sydneytv.guide.service.TvGuideService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class TvGuideViewController {

    private final TvGuideService tvGuideService;

    public TvGuideViewController(TvGuideService tvGuideService) {
        this.tvGuideService = tvGuideService;
    }

    @GetMapping("/")
    public String home(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {
        LocalDate selected = date != null ? date : tvGuideService.today();
        model.addAttribute("guide", tvGuideService.guideFor(selected));
        model.addAttribute("dates", tvGuideService.guideDates());
        model.addAttribute("selectedDate", selected);
        return "guide";
    }
}
