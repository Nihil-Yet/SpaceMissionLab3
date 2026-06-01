package spacemission.web;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import spacemission.entity.*;
import spacemission.service.MissionService;
import spacemission.service.PlanetaryData;
import spacemission.web.dto.MissionFormDto;
import spacemission.repo.LaunchSiteRepo;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/launch-sites/{launchSiteId}/missions")
public class MissionController {
    
    private final MissionService missionService;
    private final PlanetaryData planetaryData;
    private final LaunchSiteRepo launchSiteRepo;
    
    @GetMapping("/{missionId}")
    public String detailsPage(@PathVariable Long launchSiteId, 
                             @PathVariable Long missionId, 
                             Model model) {
        LaunchSite site = launchSiteRepo.findById(launchSiteId).orElse(null);
        if (site == null) return "redirect:/launch-sites";
        
        Mission mission = null;
        for (Mission m : site.getMissions()) {
            if (m.getId().equals(missionId)) {
                mission = m;
                break;
            }
        }
        
        if (mission == null) return "redirect:/launch-sites/" + launchSiteId + "/missions";
        
        model.addAttribute("launchSite", site);
        model.addAttribute("mission", mission);
        model.addAttribute("missionView", new MissionView(mission));
        model.addAttribute("actionForm", new MissionActionForm());
        
        return "missions/details";
    }
    
    @GetMapping("/create")
    public String createPage(@PathVariable Long launchSiteId,
                            @RequestParam(required = false) String type, 
                            Model model) {
        LaunchSite site = launchSiteRepo.findById(launchSiteId).orElse(null);
        if (site == null) return "redirect:/launch-sites";
        
        MissionFormDto form = new MissionFormDto();
        form.setMissionType(type != null ? type : "orbital");
        form.setLaunchSiteId(launchSiteId);
        
        model.addAttribute("launchSite", site);
        model.addAttribute("form", form);
        model.addAttribute("energySources", EnergySource.values());
        return "missions/create";
    }
    
    @PostMapping("/create")
    public String create(@PathVariable Long launchSiteId, @ModelAttribute MissionFormDto f) {
        Mission m = "orbital".equalsIgnoreCase(f.getMissionType()) 
            ? createOrbital(f) 
            : createPlanetary(f);
        
        LaunchSite site = launchSiteRepo.findById(launchSiteId).orElse(null);
        if (site != null) {
            site.addMission(m);
            missionService.save(m);
        }
        
        return "redirect:/launch-sites/" + launchSiteId + "/missions";
    }
    
    private String executeAction(Long launchSiteId, Long missionId, ActionExecutor executor, Model model) {
        LaunchSite site = launchSiteRepo.findById(launchSiteId).orElse(null);
        if (site == null) return "redirect:/launch-sites";
        
        Mission m = null;
        for (Mission mission : site.getMissions()) {
            if (mission.getId().equals(missionId)) {
                m = mission;
                break;
            }
        }
        
        if (m == null) return "redirect:/launch-sites/" + launchSiteId + "/missions";
        
        ActionResult result = executor.execute(m);
        
        model.addAttribute("launchSite", site);
        model.addAttribute("mission", m);
        model.addAttribute("missionView", new MissionView(m));
        model.addAttribute("actionForm", new MissionActionForm());
        
        if (result != null) {
            model.addAttribute("resultTitle", result.title);
            model.addAttribute("resultOutput", result.output);
        }
        return "missions/details";
    }

    @PostMapping("/{missionId}/get-info")
    public String getInfo(@PathVariable Long launchSiteId, @PathVariable Long missionId, Model model) {
        return executeAction(launchSiteId, missionId, m -> {
            if (m instanceof OrbitalMission om) om.setPlanetaryData(planetaryData);
            return new ActionResult("📄 GetInfo", m.getInfo());
        }, model);
    }
    
    @PostMapping("/{missionId}/calc-risk")
    public String calcRisk(@PathVariable Long launchSiteId, @PathVariable Long missionId, Model model) {
        return executeAction(launchSiteId, missionId, m -> {
            if (m instanceof OrbitalMission om) om.setPlanetaryData(planetaryData);
            return new ActionResult("⚠️ CalcRisk", "Риск: " + String.format("%.1f%%", m.calcRisk() * 100));
        }, model);
    }
    
    @PostMapping("/{missionId}/calc-fuel")
    public String calcFuel(@PathVariable Long launchSiteId, @PathVariable Long missionId, Model model) {
        return executeAction(launchSiteId, missionId, m -> {
            if (m instanceof OrbitalMission om) om.setPlanetaryData(planetaryData);
            return new ActionResult("⛽ CalcFuel", "Расход: " + String.format("%.2f", m.calcFuelConsumption()) + " ед.");
        }, model);
    }
    
    @PostMapping("/{missionId}/extend")
    public String extend(@PathVariable Long launchSiteId, @PathVariable Long missionId, 
                        @ModelAttribute MissionActionForm form, Model model) {
        return executeAction(launchSiteId, missionId, m -> {
            if (form.getExtendDays() == null || form.getExtendDays() <= 0) return null;
            m.extendMission(form.getExtendDays());
            missionService.save(m);
            return new ActionResult("📅 ExtendMission", "Длительность: " + m.getDuration() + " дней");
        }, model);
    }
    
    @PostMapping("/{missionId}/change-orbit")
    public String changeOrbit(@PathVariable Long launchSiteId, @PathVariable Long missionId, 
                             @RequestParam Double orbitDelta, Model model) {
        return executeAction(launchSiteId, missionId, m -> {
            if (!(m instanceof OrbitalMission om) || orbitDelta == null) return null;
            om.setPlanetaryData(planetaryData);
            Double h = om.changeOrbit(orbitDelta);
            missionService.save(m);
            return new ActionResult("🛰️ ChangeOrbit", "Новая высота: " + String.format("%.2f", h) + " км");
        }, model);
    }
    
    @PostMapping("/{missionId}/set-target")
    public String setTarget(@PathVariable Long launchSiteId, @PathVariable Long missionId, 
                           @ModelAttribute MissionActionForm form, Model model) {
        return executeAction(launchSiteId, missionId, m -> {
            if (!(m instanceof OrbitalMission om) || form.getNewTargetHeight() == null) return null;
            om.setTargetHeight(form.getNewTargetHeight());
            missionService.save(m);
            return new ActionResult("🎯 SetTarget", "Цель: " + form.getNewTargetHeight() + " км");
        }, model);
    }
    
    @PostMapping("/{missionId}/orbit-state")
    public String orbitState(@PathVariable Long launchSiteId, @PathVariable Long missionId, Model model) {
        return executeAction(launchSiteId, missionId, m -> {
            if (!(m instanceof OrbitalMission om)) return null;
            om.setPlanetaryData(planetaryData);
            OrbitState s = om.orbitState();
            return new ActionResult("🛰️ OrbitState", 
                "Высота: " + s.getCurrHeight() + " км\nТип: " + s.getType() + 
                "\nНаклон: " + s.getInclination() + "°\nПериод: " + String.format("%.1f", s.getPeriod()) + " мин");
        }, model);
    }
    
    @PostMapping("/{missionId}/set-landing")
    public String setLanding(@PathVariable Long launchSiteId, @PathVariable Long missionId, 
                            @ModelAttribute MissionActionForm form, Model model) {
        return executeAction(launchSiteId, missionId, m -> {
            if (!(m instanceof PlanetaryMission pm) || form.getNewLandingName() == null) return null;
            pm.setLandingPoint(new LandingPoint(
                form.getNewLandingName(),
                form.getNewLandingX() != null ? form.getNewLandingX().shortValue() : 0,
                form.getNewLandingY() != null ? form.getNewLandingY().shortValue() : 0,
                form.getNewLandingR() != null ? form.getNewLandingR().shortValue() : 0));
            missionService.save(m);
            return new ActionResult("📍 SetLanding", "Точка: " + form.getNewLandingName());
        }, model);
    }
    
    @PostMapping("/{missionId}/has-atmosphere")
    public String hasAtmosphere(@PathVariable Long launchSiteId, @PathVariable Long missionId, Model model) {
        return executeAction(launchSiteId, missionId, m -> {
            if (!(m instanceof PlanetaryMission pm)) return null;
            return new ActionResult("🌍 HasAtmosphere", pm.hasAtmosphere() ? "✅ Есть" : "❌ Нет");
        }, model);
    }
    
    @PostMapping("/{missionId}/scientific-goal")
    public String scientificGoal(@PathVariable Long launchSiteId, @PathVariable Long missionId, Model model) {
        return executeAction(launchSiteId, missionId, m -> {
            if (!(m instanceof PlanetaryMission pm)) return null;
            return new ActionResult("🔬 ScientificGoal", pm.scientificGoal());
        }, model);
    }

    private Mission createOrbital(MissionFormDto f) {
        String name = f.getName();
        Integer budget = f.getBudget();
        
        if ((name == null || name.isBlank()) && budget == null) {
            return new OrbitalMission();
        }
        if (name != null && !name.isBlank() && budget != null) {
            boolean hasParams = f.getCurrHeight() != null || f.getTargetHeight() != null || 
                f.getInclination() != null || f.getEnergySource() != null;
            if (!hasParams) {
                return new OrbitalMission(name, budget);
            }
        }
        return new OrbitalMission(
                name != null && !name.isBlank() ? name : "Unnamed",
                budget != null ? budget : 0,
                f.getDuration() != null ? f.getDuration() : 180,
                f.getCurrHeight() != null ? f.getCurrHeight() : 500.0,
                f.getTargetHeight() != null ? f.getTargetHeight() : 500.0,
                f.getInclination() != null ? f.getInclination() : 0.0,
                f.getEnergySource() != null ? f.getEnergySource() : EnergySource.SOLAR,
                planetaryData);
    }

    private Mission createPlanetary(MissionFormDto f) {
        String name = f.getName();
        Integer budget = f.getBudget();
        String planet = f.getPlanet();
        
        if ((name == null || name.isBlank()) && budget == null && (planet == null || planet.isBlank())) {
            return new PlanetaryMission();
        }
        if (name != null && !name.isBlank() && budget != null && planet != null && !planet.isBlank()) {
            boolean hasParams = f.getAtmoDensity() != null || f.getLandingPointName() != null;
            if (!hasParams) {
                return new PlanetaryMission(name, budget, planet); 
            }
        }
        return new PlanetaryMission(
            name != null && !name.isBlank() ? name : "Unnamed",
            budget != null ? budget : 0,
            f.getDuration() != null ? f.getDuration() : 365,
            planet != null && !planet.isBlank() ? planet : "Unknown",
            f.getAtmoDensity() != null ? f.getAtmoDensity() : 0,
            new LandingPoint(
                f.getLandingPointName() != null ? f.getLandingPointName() : "Default",
                f.getLandingPointX() != null ? f.getLandingPointX().shortValue() : 0,
                f.getLandingPointY() != null ? f.getLandingPointY().shortValue() : 0,
                f.getLandingPointR() != null ? f.getLandingPointR().shortValue() : 0));
    }
    
    @Data
    public static class MissionActionForm {
        private Integer extendDays;
        private Double orbitDelta;
        private Double newTargetHeight;
        private String newLandingName;
        private Integer newLandingX;
        private Integer newLandingY;
        private Integer newLandingR;
    }
    
    @FunctionalInterface
    private interface ActionExecutor { ActionResult execute(Mission m); }
    
    private static class ActionResult { 
        String title, output; 
        ActionResult(String t, String o) { title = t; output = o; } 
    }
    
    public static class MissionView {
        private Long id;
        private String name;
        private MissionT type;
        private Integer budget;
        private Integer duration;
        private String launchSiteName;

        public MissionView(Mission m) { 
            id = m.getId();
            name = m.getName();
            type = m.getMissionType(); 
            budget = m.getBudget();
            duration = m.getDuration(); 
            launchSiteName = m.getLaunchSite() != null ? m.getLaunchSite().getName() : null;
        }
        public Long getId() { return id; }
        public String getName() { return name; }
        public MissionT getType() { return type; }
        public Integer getBudget() { return budget; }
        public Integer getDuration() { return duration; }
        public String getLaunchSiteName() { return launchSiteName; }
    }
}
