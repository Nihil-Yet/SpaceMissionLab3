package spacemission.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import spacemission.entity.LaunchSite;
import spacemission.repo.LaunchSiteRepo;

@Controller
@RequiredArgsConstructor
@RequestMapping("/launch-sites")
public class LaunchSiteController {
    
    private final LaunchSiteRepo launchSiteRepo;
    
    @GetMapping("/create")
    public String createPage(Model model) {
        model.addAttribute("launchSite", new LaunchSite());
        return "launch-sites/create";
    }
    
    @PostMapping("/create")
    public String create(@ModelAttribute LaunchSite ls) {
        launchSiteRepo.save(ls);
        return "redirect:/missions";
    }
}
