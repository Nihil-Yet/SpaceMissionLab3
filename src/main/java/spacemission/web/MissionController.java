package spacemission.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import spacemission.entity.*;
import spacemission.service.MissionService;
import spacemission.service.PlanetaryData;
import spacemission.web.dto.MissionFormDto;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mission")
public class MissionController {
    
    private final MissionService missionService;
    private final PlanetaryData planetaryData;
    
    // === ЕДИНСТВЕННАЯ СТРАНИЦА ===
    @GetMapping
    public String page(@RequestParam(required = false) Long selectedId, Model model) {
        List<Mission> missions = missionService.findAll();
        List<MissionView> vmList = new ArrayList<>();
        
        for (Mission m : missions) {
            vmList.add(new MissionView(m));
        }
        
        Mission selected = selectedId != null ? missionService.findById(selectedId) : null;

        MissionFormDto form = new MissionFormDto();
        form.setMissionType("orbital");
        
        model.addAttribute("missions", vmList);
        model.addAttribute("selectedId", selectedId);
        model.addAttribute("selectedMission", selected);
        model.addAttribute("form", new MissionFormDto());
        model.addAttribute("energySources", EnergySource.values());
        return "mission";
    }
    
    // === СОЗДАНИЕ ===
    @PostMapping("/create")
    public String create(@ModelAttribute MissionFormDto f, Model model) {
        boolean isOrbital = "orbital".equalsIgnoreCase(f.getMissionType());
        Mission m = isOrbital ? createOrbital(f) : createPlanetary(f);
        missionService.save(m);
        return "redirect:/mission?selectedId=" + m.getId();
    }
    
    // === МЕТОДЫ: ИНФОРМАЦИЯ ===
    @PostMapping("/get-info")
    public String getInfo(@RequestParam Long selectedId, Model model) {
        return executeMethod(selectedId, m -> {
            if (m instanceof OrbitalMission om) om.setPlanetaryData(planetaryData);
            return new MethodResult("📄 GetInfo", m.getInfo());
        }, model);
    }
    
    @PostMapping("/calc-risk")
    public String calcRisk(@RequestParam Long selectedId, Model model) {
        return executeMethod(selectedId, m -> {
            if (m instanceof OrbitalMission om) om.setPlanetaryData(planetaryData);
            return new MethodResult("⚠️ CalcRisk", "Риск: " + String.format("%.1f%%", m.calcRisk() * 100));
        }, model);
    }
    
    @PostMapping("/calc-fuel")
    public String calcFuel(@RequestParam Long selectedId, Model model) {
        return executeMethod(selectedId, m -> {
            if (m instanceof OrbitalMission om) om.setPlanetaryData(planetaryData);
            return new MethodResult("⛽ CalcFuel", "Расход: " + String.format("%.2f", m.calcFuelConsumption()) + " ед.");
        }, model);
    }
    
    // === МЕТОДЫ: ИЗМЕНЕНИЕ ===
    @PostMapping("/extend")
    public String extend(@RequestParam Long selectedId, @RequestParam Integer extendDays, Model model) {
        return executeMethod(selectedId, m -> {
            if (extendDays == null || extendDays <= 0) return null;
            m.extendMission(extendDays);
            missionService.save(m);
            return new MethodResult("📅 ExtendMission", "Длительность: " + m.getDuration() + " дней");
        }, model);
    }
    
    @PostMapping("/change-orbit")
    public String changeOrbit(@RequestParam Long selectedId, @RequestParam Double orbitDelta, Model model) {
        return executeMethod(selectedId, m -> {
            if (!(m instanceof OrbitalMission om) || orbitDelta == null) return null;
            om.setPlanetaryData(planetaryData);
            Double h = om.changeOrbit(orbitDelta);
            missionService.save(m);
            return new MethodResult("🛰️ ChangeOrbit", "Высота: " + String.format("%.2f", h) + " км");
        }, model);
    }
    
    @PostMapping("/set-target")
    public String setTarget(@RequestParam Long selectedId, @RequestParam Double newTargetHeight, Model model) {
        return executeMethod(selectedId, m -> {
            if (!(m instanceof OrbitalMission om) || newTargetHeight == null) return null;
            om.setTargetHeight(newTargetHeight);
            missionService.save(m);
            return new MethodResult("🎯 SetTarget", "Цель: " + newTargetHeight + " км");
        }, model);
    }
    
    @PostMapping("/orbit-state")
    public String orbitState(@RequestParam Long selectedId, Model model) {
        return executeMethod(selectedId, m -> {
            if (!(m instanceof OrbitalMission om)) return null;
            om.setPlanetaryData(planetaryData);
            OrbitState s = om.orbitState();
            return new MethodResult("🛰️ OrbitState", 
                "Высота: " + s.getCurrHeight() + " км\nТип: " + s.getType() + 
                "\nНаклон: " + s.getInclination() + "°\nПериод: " + String.format("%.1f", s.getPeriod()) + " мин");
        }, model);
    }
    
    @PostMapping("/set-landing")
    public String setLanding(@RequestParam Long selectedId,
                            @RequestParam String newLandingName,
                            @RequestParam(required = false) Integer newLandingX,
                            @RequestParam(required = false) Integer newLandingY,
                            @RequestParam(required = false) Integer newLandingR, Model model) {
        return executeMethod(selectedId, m -> {
            if (!(m instanceof PlanetaryMission pm) || newLandingName == null) return null;
            pm.setLandingPoint(new LandingPoint(
                newLandingName,
                newLandingX != null ? newLandingX.shortValue() : 0,
                newLandingY != null ? newLandingY.shortValue() : 0,
                newLandingR != null ? newLandingR.shortValue() : 0));
            missionService.save(m);
            return new MethodResult("📍 SetLanding", "Точка: " + newLandingName);
        }, model);
    }
    
    @PostMapping("/has-atmosphere")
    public String hasAtmosphere(@RequestParam Long selectedId, Model model) {
        return executeMethod(selectedId, m -> {
            if (!(m instanceof PlanetaryMission pm)) return null;
            return new MethodResult("🌍 HasAtmosphere", pm.hasAtmosphere() ? "✅ Есть" : "❌ Нет");
        }, model);
    }
    
    @PostMapping("/scientific-goal")
    public String scientificGoal(@RequestParam Long selectedId, Model model) {
        return executeMethod(selectedId, m -> {
            if (!(m instanceof PlanetaryMission pm)) return null;
            return new MethodResult("🔬 ScientificGoal", pm.scientificGoal());
        }, model);
    }
    
    // === ВСПОМОГАТЕЛЬНЫЕ ===
    
    private String executeMethod(Long selectedId, MethodExecutor executor, Model model) {
        Mission m = missionService.findById(selectedId);
        MethodResult result = (m != null) ? executor.execute(m) : null;
        
        List<MissionView> vmList = new ArrayList<>();
        for (Mission mission : missionService.findAll()) {
            vmList.add(new MissionView(mission));
        }
        
        model.addAttribute("missions", vmList);
        model.addAttribute("selectedId", selectedId);
        model.addAttribute("selectedMission", m);
        model.addAttribute("form", new MissionFormDto());
        model.addAttribute("energySources", EnergySource.values());
        
        if (result != null) {
            model.addAttribute("resultTitle", result.title);
            model.addAttribute("resultOutput", result.output);
        }
        return "mission";
    }

    private Mission createOrbital(MissionFormDto f) {
        String name = f.getName();
        Integer budget = f.getBudget();
        // 🔹 Конструктор #0: no-arg (если всё пустое)
        if ((name == null || name.isBlank()) && budget == null) {
            return new OrbitalMission();
        }
        // 🔹 Конструктор #1: минимальный (name + budget)
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
        // 🔹 Конструктор #0: no-arg (если всё пустое)
        if ((name == null || name.isBlank()) && budget == null && (planet == null || planet.isBlank())) {
            return new PlanetaryMission();
        }
        // 🔹 Конструктор #1: минимальный (name + budget + planet)
        if (name != null && !name.isBlank() && budget != null && planet != null && !planet.isBlank()) {
            boolean hasParams = f.getAtmoDensity() != null || f.getLandingPointName() != null;
            if (!hasParams) {
                return new PlanetaryMission(name, budget, planet); 
            }
        }
        // 🔹 Конструктор #2: полный
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
    
    // === DTO для вида ===
    public static class MissionView {
        private Long id; private String name; private MissionT type; private Integer budget; private Integer duration;
        public MissionView(Mission m) { id = m.getId(); name = m.getName(); type = m.getMissionType(); budget = m.getBudget(); duration = m.getDuration(); }
        public Long getId() { return id; } public String getName() { return name; } public MissionT getType() { return type; }
        public Integer getBudget() { return budget; } public Integer getDuration() { return duration; }
    }
    
    @FunctionalInterface
    private interface MethodExecutor { MethodResult execute(Mission m); }
    
    private static class MethodResult { String title, output; MethodResult(String t, String o) { title = t; output = o; } }
}
