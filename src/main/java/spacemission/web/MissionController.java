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

@Controller
@RequiredArgsConstructor
@RequestMapping("/missions")
public class MissionController {
    
    private final MissionService missionService;
    private final PlanetaryData planetaryData;
    private final LaunchSiteRepo launchSiteRepo;
    
    @GetMapping
    public String rootRedirect() {
        return "redirect:/launch-sites";
    }
    
    @GetMapping("/{id}")
    public String detailsPage(@PathVariable Long id, Model model) {
        Mission mission = missionService.findById(id);
        if (mission == null) return "redirect:/launch-sites";
        
        model.addAttribute("mission", mission);
        model.addAttribute("missionView", new MissionView(mission));
        model.addAttribute("actionForm", new MissionActionForm());
        model.addAttribute("launchSites", launchSiteRepo.findAll());
        
        return "missions/details";
    }
    
    @GetMapping("/create")
    public String createPage(@RequestParam(required = false) Long launchSiteId, 
                            @RequestParam(required = false) String type, 
                            Model model) {
        MissionFormDto form = new MissionFormDto();
        form.setMissionType(type != null ? type : "orbital");
        if (launchSiteId != null) form.setLaunchSiteId(launchSiteId);
        
        model.addAttribute("form", form);
        model.addAttribute("energySources", EnergySource.values());
        model.addAttribute("launchSites", launchSiteRepo.findAll());
        return "missions/create";
    }
    
    @PostMapping("/create")
    public String create(@ModelAttribute MissionFormDto f) {
        Mission m = "orbital".equalsIgnoreCase(f.getMissionType()) 
            ? createOrbital(f) 
            : createPlanetary(f);
        
        if (f.getLaunchSiteId() != null) {
            launchSiteRepo.findById(f.getLaunchSiteId()).ifPresent(m::setLaunchSite);
        }
        
        missionService.save(m);
        Long redirectSiteId = f.getLaunchSiteId() != null ? f.getLaunchSiteId() : 1L;
        return "redirect:/launch-sites/" + redirectSiteId + "/missions";
    }
    
    @PostMapping("/{id}/get-info")
    public String getInfo(@PathVariable Long id, Model model) {
        return executeAction(id, m -> {
            if (m instanceof OrbitalMission om) om.setPlanetaryData(planetaryData);
            return new ActionResult("📄 GetInfo", m.getInfo());
        }, model);
    }
    
    @PostMapping("/{id}/calc-risk")
    public String calcRisk(@PathVariable Long id, Model model) {
        return executeAction(id, m -> {
            if (m instanceof OrbitalMission om) om.setPlanetaryData(planetaryData);
            return new ActionResult("⚠️ CalcRisk", "Риск: " + String.format("%.1f%%", m.calcRisk() * 100));
        }, model);
    }
    
    @PostMapping("/{id}/calc-fuel")
    public String calcFuel(@PathVariable Long id, Model model) {
        return executeAction(id, m -> {
            if (m instanceof OrbitalMission om) om.setPlanetaryData(planetaryData);
            return new ActionResult("⛽ CalcFuel", "Расход: " + String.format("%.2f", m.calcFuelConsumption()) + " ед.");
        }, model);
    }
    
    @PostMapping("/{id}/extend")
    public String extend(@PathVariable Long id, @ModelAttribute MissionActionForm form, Model model) {
        return executeAction(id, m -> {
            if (form.getExtendDays() == null || form.getExtendDays() <= 0) return null;
            m.extendMission(form.getExtendDays());
            missionService.save(m);
            return new ActionResult("📅 ExtendMission", "Длительность: " + m.getDuration() + " дней");
        }, model);
    }
    
    @PostMapping("/{id}/change-orbit")
    public String changeOrbit(@PathVariable Long id, @RequestParam Double orbitDelta, Model model) {
        return executeAction(id, m -> {
            if (!(m instanceof OrbitalMission om) || orbitDelta == null) return null;
            om.setPlanetaryData(planetaryData);
            Double h = om.changeOrbit(orbitDelta);
            missionService.save(m);
            return new ActionResult("🛰️ ChangeOrbit", "Новая высота: " + String.format("%.2f", h) + " км");
        }, model);
    }
    
    @PostMapping("/{id}/set-target")
    public String setTarget(@PathVariable Long id, @ModelAttribute MissionActionForm form, Model model) {
        return executeAction(id, m -> {
            if (!(m instanceof OrbitalMission om) || form.getNewTargetHeight() == null) return null;
            om.setTargetHeight(form.getNewTargetHeight());
            missionService.save(m);
            return new ActionResult("🎯 SetTarget", "Цель: " + form.getNewTargetHeight() + " км");
        }, model);
    }
    
    @PostMapping("/{id}/orbit-state")
    public String orbitState(@PathVariable Long id, Model model) {
        return executeAction(id, m -> {
            if (!(m instanceof OrbitalMission om)) return null;
            om.setPlanetaryData(planetaryData);
            OrbitState s = om.orbitState();
            return new ActionResult("🛰️ OrbitState", 
                "Высота: " + s.getCurrHeight() + " км\nТип: " + s.getType() + 
                "\nНаклон: " + s.getInclination() + "°\nПериод: " + String.format("%.1f", s.getPeriod()) + " мин");
        }, model);
    }
    
    @PostMapping("/{id}/set-landing")
    public String setLanding(@PathVariable Long id, @ModelAttribute MissionActionForm form, Model model) {
        return executeAction(id, m -> {
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
    
    @PostMapping("/{id}/has-atmosphere")
    public String hasAtmosphere(@PathVariable Long id, Model model) {
        return executeAction(id, m -> {
            if (!(m instanceof PlanetaryMission pm)) return null;
            return new ActionResult("🌍 HasAtmosphere", pm.hasAtmosphere() ? "✅ Есть" : "❌ Нет");
        }, model);
    }
    
    @PostMapping("/{id}/scientific-goal")
    public String scientificGoal(@PathVariable Long id, Model model) {
        return executeAction(id, m -> {
            if (!(m instanceof PlanetaryMission pm)) return null;
            return new ActionResult("🔬 ScientificGoal", pm.scientificGoal());
        }, model);
    }
    
    private String executeAction(Long id, ActionExecutor executor, Model model) {
        Mission m = missionService.findById(id);
        ActionResult result = (m != null) ? executor.execute(m) : null;
        
        if (m != null) {
            model.addAttribute("mission", m);
            model.addAttribute("missionView", new MissionView(m));
        }
        model.addAttribute("actionForm", new MissionActionForm());
        model.addAttribute("launchSites", launchSiteRepo.findAll());
        
        if (result != null) {
            model.addAttribute("resultTitle", result.title);
            model.addAttribute("resultOutput", result.output);
        }
        return "missions/details";
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
