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
    
    // === Выбор типа: ТОЛЬКО String/Boolean для формы ===
    private String missionType;  // "orbital" или "planetary" — для биндинга из HTML
    // ❌ УБРАНО: private MissionT missionType; — enum не биндится из строки без конвертера
    
    // === Orbital поля ===
    private Double currHeight;
    private Double targetHeight;
    private Double inclination;
    private EnergySource energySource;
    
    // === Planetary поля ===
    private String planet;
    private Integer atmoDensity;  // ✅ Integer, не Byte (0..255)
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
    
    // === Helpers: определяем тип по строке из формы ===
    public boolean isOrbitalType() {
        return "orbital".equalsIgnoreCase(missionType);
    }
    public boolean isPlanetaryType() {
        return "planetary".equalsIgnoreCase(missionType);
    }
}
