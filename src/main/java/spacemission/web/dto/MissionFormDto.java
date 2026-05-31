package spacemission.web.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import spacemission.entity.EnergySource;

@Data
@NoArgsConstructor
public class MissionFormDto {
    // === Идентификатор ===
    private Long id;
    
    // === Базовые поля миссии ===
    private String name;
    private Integer budget;
    private Integer duration;
    
    // === Выбор типа миссии (строка для биндинга из HTML) ===
    private String missionType;  // "orbital" или "planetary"
    
    // === привязка к стартовой площадке ===
    private Long launchSiteId;
    
    // === Orbital поля ===
    private Double currHeight;
    private Double targetHeight;
    private Double inclination;
    private EnergySource energySource;
    
    // === Planetary поля ===
    private String planet;
    private Integer atmoDensity;
    private String landingPointName;
    private Integer landingPointX;
    private Integer landingPointY;
    private Integer landingPointR;
    
    // === Параметры для методов ===
    private Integer extendDays;
    private Double orbitDelta;
    private Double newTargetHeight;
    private String newLandingName;
    private Integer newLandingX;
    private Integer newLandingY;
    private Integer newLandingR;
    
    // === Результат ===
    private String resultTitle;
    private String resultOutput;
    
    // === Helpers ===
    public boolean isOrbitalType() {
        return "orbital".equalsIgnoreCase(missionType);
    }
    public boolean isPlanetaryType() {
        return "planetary".equalsIgnoreCase(missionType);
    }
}
