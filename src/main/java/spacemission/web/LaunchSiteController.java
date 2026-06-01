package spacemission.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import spacemission.entity.LaunchSite;
import spacemission.entity.Mission;
import spacemission.service.MissionService;
import spacemission.repo.LaunchSiteRepo;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/launch-sites")
public class LaunchSiteController {
    
    private final LaunchSiteRepo launchSiteRepo;
    private final MissionService missionService;
    
    @GetMapping
    public String listPage(Model model) {
        Iterable<LaunchSite> sitesIterable = launchSiteRepo.findAll();
        List<LaunchSite> sites = new java.util.ArrayList<>();
        sitesIterable.forEach(sites::add);
        model.addAttribute("launchSites", sites);
        return "launch-sites/list";
    }
    
    @GetMapping("/create")
    public String createPage(Model model) {
        model.addAttribute("launchSite", new LaunchSite());
        return "launch-sites/create";
    }
    
    @PostMapping("/create")
    public String create(@ModelAttribute LaunchSite ls) {
        launchSiteRepo.save(ls);
        return "redirect:/launch-sites";
    }
    
    @GetMapping("/{id}/missions")
    public String missionsBySite(@PathVariable Long id, Model model) {
        LaunchSite site = launchSiteRepo.findById(id).orElse(null);
        if (site == null) return "redirect:/launch-sites";
        
        List<Mission> missions = new java.util.ArrayList<>(site.getMissions());
        
        List<MissionController.MissionView> vmList = missions.stream()
            .map(MissionController.MissionView::new)
            .collect(Collectors.toList());
        
        model.addAttribute("launchSite", site);
        model.addAttribute("missions", vmList);
        return "launch-sites/missions";
    }
}
